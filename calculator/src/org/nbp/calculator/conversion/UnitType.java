package org.nbp.calculator.conversion;

import java.util.Map;
import java.util.HashMap;

public class UnitType {
  private final String typeName;
  private final Unit baseUnit;

  private final static Map<String, UnitType> unitTypes
             = new HashMap<String, UnitType>();

  public UnitType (String name, boolean international, String... unit) {
    if (unitTypes.containsKey(name)) {
      throw new UnitException(("duplicate unit type: " + name));
    }

    if (international) {
      baseUnit = new InternationalUnit(this, unit);
    } else {
      baseUnit = new Unit(this, unit);
    }

    typeName = name;
    unitTypes.put(typeName, this);
  }

  public final String getName () {
    return typeName;
  }

  public final Unit getBaseUnit () {
    return baseUnit;
  }

  public final static UnitType get (String name) {
    UnitType type = unitTypes.get(name);
    if (type != null) return type;
    throw new UnitException(("unknown unit type: " + name));
  }
}