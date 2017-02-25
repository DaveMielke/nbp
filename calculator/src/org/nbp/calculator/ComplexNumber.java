package org.nbp.calculator;

public class ComplexNumber extends ComplexCommon {
  private final double real;
  private final double imag;

  public ComplexNumber (double r, double i) {
    super();

    real = r;
    imag = i;
  }

  public ComplexNumber (double r) {
    this(r, ZERO);
  }

  public final double real () {
    return real;
  }

  public final double imag () {
    return imag;
  }

  public final double abs () {
    if (imag == ZERO) return Math.abs(real);
    if (real == ZERO) return Math.abs(imag);
    return Math.hypot(real, imag);
  }

  public final double arg () {
    return Math.atan2(imag, real);
  }

  public final boolean hasReal () {
    return real != ZERO;
  }

  public final boolean hasImag () {
    return imag != ZERO;
  }

  private final boolean isReal (ComplexNumber operand) {
    return !(hasImag() || operand.hasImag());
  }

  public final static ComplexNumber NaN = new ComplexNumber(Double.NaN, Double.NaN);

  public final boolean isNaN () {
    return Double.isNaN(real) || Double.isNaN(imag);
  }

  public final boolean equals (ComplexNumber operand) {
    if (isNaN()) return operand.isNaN();
    if (operand.isNaN()) return false;
    return ((real == operand.real) && (imag == operand.imag));
  }

  public final ComplexNumber neg () {
    return new ComplexNumber(-real, -imag);
  }

  public final ComplexNumber cnj () {
    return new ComplexNumber(real, -imag);
  }

  public final ComplexNumber rcp () {
    if (imag == ZERO) return new ComplexNumber(1d / real);

    double denominator = (real * real) + (imag * imag);
    return new ComplexNumber((real / denominator), (-imag / denominator));
  }

  public final ComplexNumber add (ComplexNumber addend) {
    return new ComplexNumber((real + addend.real), (imag + addend.imag));
  }

  public final ComplexNumber add (double addend) {
    return add(new ComplexNumber(addend));
  }

  public final ComplexNumber sub (ComplexNumber subtrahend) {
    return new ComplexNumber((real - subtrahend.real), (imag - subtrahend.imag));
  }

  public final ComplexNumber sub (double subtrahend) {
    return sub(new ComplexNumber(subtrahend));
  }

  public final ComplexNumber mul (ComplexNumber multiplier) {
    return new ComplexNumber(
      (real * multiplier.real) - (imag * multiplier.imag),
      (real * multiplier.imag) + (imag * multiplier.real)
    );
  }

  public final ComplexNumber mul (double multiplier) {
    return mul(new ComplexNumber(multiplier));
  }

  public final ComplexNumber div (ComplexNumber divisor) {
    if (isReal(divisor)) return new ComplexNumber(real / divisor.real);
    return mul(divisor.rcp());
  }

  public final ComplexNumber div (double divisor) {
    return div(new ComplexNumber(divisor));
  }

  public final ComplexNumber log () {
    return new ComplexNumber(Math.log(abs()), arg());
  }

  public final ComplexNumber log (double base) {
    return log().div(new ComplexNumber(Math.log(base)));
  }

  public final ComplexNumber exp () {
    double factor = Math.exp(real);
    if (imag == ZERO) return new ComplexNumber(factor);

    return new ComplexNumber(
      factor * Math.cos(imag),
      factor * Math.sin(imag)
    );
  }

  public final ComplexNumber pow (ComplexNumber exponent) {
    if (isReal(exponent)) return new ComplexNumber(Math.pow(real, exponent.real));
    return exponent.mul(log()).exp();
  }

  public final ComplexNumber pow (double exponent) {
    return pow(new ComplexNumber(exponent));
  }

  public final String format () {
    return SavedSettings.getNotation().getFormatter().format(real, imag);
  }

  private final static char STRING_DELIMITER = '_';

  public final String toString() {
    return (Double.toString(real) + STRING_DELIMITER + Double.toString(imag));
  }

  public final static ComplexNumber valueOf (String string) {
    int delimiter = string.indexOf(STRING_DELIMITER);
    if (delimiter < 0) return new ComplexNumber(Double.valueOf(string));

    return new ComplexNumber(
      Double.valueOf(string.substring(0, delimiter)),
      Double.valueOf(string.substring(delimiter+1))
    );
  }
}
