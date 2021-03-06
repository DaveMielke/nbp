package org.nbp.common.dictionary;

import android.util.Log;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import java.io.Closeable;
import java.io.IOException;

import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DictionaryConnection implements Closeable {
  private final static String LOG_TAG = DictionaryConnection.class.getName();

  private final static Object IDENTIFIER_LOCK = new Object();
  private static int previousIdentifier = 0;
  private final int currentIdentifier;

  private DictionaryConnection () {
    synchronized (IDENTIFIER_LOCK) {
      currentIdentifier = ++previousIdentifier;
    }
  }

  public final int getIdentifier () {
    return currentIdentifier;
  }

  private final void logEvent (String event) {
    Log.d(LOG_TAG,
      String.format(
        "connection %d: %s", getIdentifier(), event
      )
    );
  }

  private final static Object GET_LOCK = new Object();
  private static DictionaryConnection currentConnection = null;

  public static DictionaryConnection get () {
    synchronized (GET_LOCK) {
      if (currentConnection == null) currentConnection = new DictionaryConnection();
      return currentConnection;
    }
  }

  private Socket clientSocket = null;
  private Writer commandWriter = null;
  private BufferedReader responseReader = null;

  private Thread requestThread = null;
  private Thread responseThread = null;

  private class RequestEntry {
    public final RequestHandler handler;
    public final String[] arguments;

    public RequestEntry (RequestHandler handler, String... arguments) {
      this.handler = handler;
      this.arguments = arguments;
    }
  }

  private final BlockingQueue<RequestEntry> requestQueue =
      new LinkedBlockingQueue<RequestEntry>();

  private final BlockingQueue<RequestHandler> responseQueue =
      new LinkedBlockingQueue<RequestHandler>();

  private final void flushRequestQueue () {
    while (true) {
      RequestEntry request = requestQueue.poll();
      if (request == null) break;
      request.handler.setFinished();
    }
  }

  private final void flushResponseQueue () {
    while (true) {
      RequestHandler handler = responseQueue.poll();
      if (handler == null) break;
      handler.setFinished();
    }
  }

  private static void close (Closeable closeable, String what) {
    try {
      closeable.close();
    } catch (IOException exception) {
      Log.w(LOG_TAG,
        String.format(
          "%s close error: %s",
          what, exception.getMessage()
        )
      );
    }
  }

  private final void closeWriter () {
    if (commandWriter != null) {
      close(commandWriter, "writer");
      commandWriter = null;
    }
  }

  private final void closeReader () {
    if (responseReader != null) {
      close(responseReader, "reader");
      responseReader = null;
    }
  }

  private final void closeSocket () {
    if (clientSocket != null) {
      if (!clientSocket.isClosed()) {
        logEvent("client disconnecting");

        try {
          closeReader();
          closeWriter();

          try {
            clientSocket.close();
          } catch (IOException exception) {
            Log.w(LOG_TAG, ("socket close error: " + exception.getMessage()));
          }
        } finally {
          logEvent("client disconnected");
        }
      }
    }
  }

  @Override
  public void close () {
    synchronized (GET_LOCK) {
      if (this == currentConnection) currentConnection = null;
    }

    synchronized (this) {
      closeSocket();
      flushResponseQueue();
    }
  }

  private static SocketAddress makeServerAddress () {
    return new InetSocketAddress(
      DictionaryParameters.SERVER_NAME,
      DictionaryParameters.SERVER_PORT
    );
  }

  private final Socket getSocket () {
    if (clientSocket == null) {
      Socket socket = new Socket();

      try {
        logEvent("client connecting");
        socket.connect(makeServerAddress());
        logEvent("client connected");
        clientSocket = socket;
      } catch (IOException exception) {
        Log.e(LOG_TAG, ("client connect error: " + exception.getMessage()));
      }
    } else if (clientSocket.isClosed()) {
      return null;
    }

    return clientSocket;
  }

  private final Writer getWriter () {
    if (commandWriter == null) {
      Socket socket = getSocket();

      if (socket != null) {
        try {
          OutputStream stream = socket.getOutputStream();
          commandWriter = new OutputStreamWriter(stream, DictionaryParameters.CHARACTER_ENCODING);
        } catch (IOException exception) {
          Log.e(LOG_TAG, ("writer creation error: " + exception.getMessage()));
        }
      }
    }

    return commandWriter;
  }

  private final BufferedReader getReader () {
    if (responseReader == null) {
      Socket socket = getSocket();

      if (socket != null) {
        try {
          InputStream stream = socket.getInputStream();
          Reader reader = new InputStreamReader(stream, DictionaryParameters.CHARACTER_ENCODING);
          responseReader = new BufferedReader(reader);
        } catch (IOException exception) {
          Log.e(LOG_TAG, ("reader creation error: " + exception.getMessage()));
        }
      }
    }

    return responseReader;
  }

  public final String readLine () {
    BufferedReader reader = getReader();
    if (reader == null) return null;

    try {
      String line = reader.readLine();
      if (line != null) return line;
      Log.w(LOG_TAG, "server disconnected");
    } catch (IOException exception) {
      Log.e(LOG_TAG, ("socket read error: " + exception.getMessage()));
    }

    return null;
  }

  private final void handleBanner (DictionaryOperands operands) {
  }

  private final boolean handleResponse (int code, DictionaryOperands operands) {
    switch (code) {
      case ResponseCodes.SERVER_BANNER:
        handleBanner(operands);
        return true;

      default:
        return false;
    }
  }

  private final int parseResponseCode (String operand) {
    int value;

    try {
      value = Integer.parseInt(operand, 10);

      if (!Character.isDigit(operand.charAt(0))) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException exception) {
      throw new OperandException(
        "response code is not an integer", operand
      );
    }

    if (operand.length() != 3) {
      throw new OperandException(
        "response code is not three digits", operand
      );
    }

    return value;
  }

  private final void runResponseLoop () {
    while (true) {
      String response = readLine();
      if (response == null) break;
      Log.i(LOG_TAG, ("response: " + response));

      try {
        DictionaryOperands operands = new DictionaryOperands(response);

        if (operands.isEmpty()) {
          throw new OperandException("missing response code");
        }

        int code = parseResponseCode(operands.removeFirst());
        if (handleResponse(code, operands)) continue;
        RequestHandler handler;

        synchronized (this) {
          handler = responseQueue.peek();
        }

        if (handler == null) {
          throw new OperandException("no request handler");
        }

        if (handler.handleResponse(code, operands)) {
          responseQueue.remove();
          handler.setFinished();
        }
      } catch (OperandException exception) {
        Log.w(LOG_TAG, exception.getMessage());
      }
    }
  }

  private final void startResponseThread () {
    synchronized (this) {
      if ((responseThread == null) || !responseThread.isAlive()) {
        responseThread = new Thread("dictionary-response-thread") {
          @Override
          public void run () {
            logEvent("response thread starting");

            try {
              runResponseLoop();
            } finally {
              logEvent("response thread finished");
              requestThread.interrupt();
              close();
            }
          }
        };

        responseThread.start();
      }
    }
  }

  private final void runRequestLoop () {
    StringBuilder command = new StringBuilder();

    while (true) {
      RequestEntry request;

      try {
        request = requestQueue.take();
        if (request == null) break;
      } catch (InterruptedException exception) {
        logEvent("request thread interrupted");
        break;
      }

      RequestHandler handler = request.handler;
      boolean isFinal = handler.isFinal();

      try {
        try {
          command.setLength(0);
          String[] arguments = request.arguments;

          if (arguments.length == 0) {
            throw new OperandException("missing command");
          }

          for (String argument : arguments) {
            if (command.length() > 0) command.append(' ');
            command.append(DictionaryOperands.quote(argument));
          }

          Log.d(LOG_TAG, ("command: " + command));
          command.append("\r\n");

          {
            int maximum = DictionaryParameters.MAXIMUM_LENGTH;
            int length = command.length();

            if (length > maximum) {
              throw new OperandException(
                String.format(
                  "command line too long: %d > %d",
                  length, maximum
                )
              );
            }
          }

          synchronized (this) {
            Writer writer = getWriter();
            if (writer == null) break;

            try {
              writer.write(command.toString());
              writer.flush();
            } catch (IOException exception) {
              Log.e(LOG_TAG, ("socket write error: " + exception.getMessage()));
              close();
              break;
            }

            responseQueue.offer(handler);
            handler = null;
            startResponseThread();
          }
        } catch (OperandException exception) {
          Log.w(LOG_TAG, exception.getMessage());
        }

        if (isFinal) break;
      } finally {
        if (handler != null) handler.setFinished();
      }
    }
  }

  private final void startRequestThread () {
    synchronized (this) {
      if ((requestThread == null) || !requestThread.isAlive()) {
        requestThread = new Thread("dictionary-request-thread") {
          @Override
          public void run () {
            logEvent("request thread starting");

            try {
              runRequestLoop();
            } finally {
              flushRequestQueue();
              logEvent("request thread finished");
            }
          }
        };

        requestThread.start();
      }
    }
  }

  public final void enqueueRequest (RequestHandler handler, String... arguments) {
    startRequestThread();
    requestQueue.offer(new RequestEntry(handler, arguments));
  }
}
