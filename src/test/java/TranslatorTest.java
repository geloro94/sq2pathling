import de.fdpg.sq2pathling.Translator;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.StructuredQuery;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TranslatorTest {

  @Test
  void toPathling() {
    var Parameters = Translator.of()
        .toPathling(StructuredQuery.of(List.of(List.of(Criterion.TRUE))));

  }

}
