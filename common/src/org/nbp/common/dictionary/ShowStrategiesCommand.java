package org.nbp.common.dictionary;

public class ShowStrategiesCommand extends ItemsRequest {
  public ShowStrategiesCommand () {
    super("show", "strategies");
  }

  @Override
  protected final int getResponseCode () {
    return ResponseCodes.BEGIN_STRATEGY_LIST;
  }
}
