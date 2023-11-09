package de.fdpg.sq2pathling.model.structured_query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.structured_query.CodeModifier;
import org.junit.jupiter.api.Test;

class CodeModifierTest {

  @Test
  void expression_OneCode() {
    var modifier = CodeModifier.of("status", "final");

    var expression = modifier.expression(MappingContext.of());

    assertEquals("status = 'final'", PrintContext.ZERO.print(expression));
  }

  @Test
  void expression_TwoCodes() {
    var modifier = CodeModifier.of("status", "completed", "in-progress");

    var expression = modifier.expression(MappingContext.of());

    assertEquals("""
        status = 'completed' or
        status = 'in-progress'""", PrintContext.ZERO.print(expression));
  }
}