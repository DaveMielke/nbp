package org.nbp.calculator.functions;

import org.nbp.calculator.ComplexOperations;
import org.nbp.calculator.ComplexNumber;
import static org.nbp.calculator.ComplexNumber.ONE;
import static org.nbp.calculator.ComplexNumber.PI;

// Cornelius Lanczos 1964 approximation of the Gamma function
//
public abstract class Gamma {
  private final static ComplexNumber[] coefficientArray = {
    new ComplexNumber(676.5203681218851d),
    new ComplexNumber(-1259.1392167224028d),
    new ComplexNumber(771.32342877765313d),
    new ComplexNumber(-176.61502916214059d),
    new ComplexNumber(12.507343278686905d),
    new ComplexNumber(-0.13857109526572012d),
    new ComplexNumber(9.9843695780195716e-6d),
    new ComplexNumber(1.5056327351493116e-7d)
  };

  private final static int coefficientCount = coefficientArray.length;
  private final static double EPSILON = 0.0000001d;

  public final static ComplexNumber gamma (ComplexNumber number) {
    ComplexNumber result;

    if (number.real() < 0.5d) {
      result = PI.div(number.mul(PI).sin().mul(ONE.sub(number).gamma()));
    } else {
      number = number.sub(1d);
      ComplexNumber x = new ComplexNumber(0.99999999999980993d);
      int coefficientNumber = 0;

      while (coefficientNumber < coefficientCount) {
        ComplexNumber coefficient = coefficientArray[coefficientNumber];
        x = x.add(coefficient.div(number.add(++coefficientNumber)));
      }

      ComplexNumber t = number.add((double)coefficientCount - 0.5d);
      result = ComplexOperations.sqrt(PI.mul(2d))
                                .mul(t.pow(number.add(0.5d)))
                                .mul(t.neg().exp()).mul(x);
    }

    if (Math.abs(result.imag()) < EPSILON) result = new ComplexNumber(result.real());
    return result;
  }

  private Gamma () {
  }
}
