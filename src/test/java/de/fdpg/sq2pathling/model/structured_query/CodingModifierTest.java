package de.fdpg.sq2pathling.model.structured_query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.CodingModifier;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CodingModifierTest {

  static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
      "confirmed", "Conformed");

  static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
      "http://terminology.hl7.org/CodeSystem/condition-ver-status", "ver_status");

  static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(), null);

  @Test
  void expression() {
    var modifier = CodingModifier.of("verificationStatus", CONFIRMED);

    var expression = modifier.expression(MAPPING_CONTEXT);

    assertEquals("verificationStatus.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed')",
        expression.print(PrintContext.ZERO));
  }
}