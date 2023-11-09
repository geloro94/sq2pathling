package de.fdpg.sq2pathling.model;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.Concept;
import de.fdpg.sq2pathling.model.structured_query.ContextualConcept;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A context holding information to facilitate the mapping process.
 *
 * @author Lorenz
 */
public class MappingContext {

  private final Map<ContextualTermCode, Mapping> mappings;
  private final TermCodeNode conceptTree;

  private MappingContext(Map<ContextualTermCode, Mapping> mappings, TermCodeNode conceptTree) {
    this.mappings = mappings;
    this.conceptTree = conceptTree;
  }

  /**
   * Returns an empty mapping context.
   *
   * @return the mapping context
   */
  public static MappingContext of() {
    return new MappingContext(Map.of(), null);
  }

  /**
   * Returns a mapping context.
   *
   * @param mappings    the mappings keyed by their term code
   * @param conceptTree a tree of concepts to expand (can be null)
   * @return the mapping context
   */
  public static MappingContext of(Map<ContextualTermCode, Mapping> mappings, TermCodeNode conceptTree) {
    return new MappingContext(Map.copyOf(mappings), conceptTree);
  }

  /**
   * Tries to find the {@link Mapping} with the given {@code key}.
   *
   * @param key the TermCode of the mapping
   * @return either the Mapping or {@code Optional#empty() nothing}
   */
  public Optional<Mapping> findMapping(ContextualTermCode key) {
    return Optional.ofNullable(mappings.get(requireNonNull(key)));
  }

  /**
   * Expands {@code concept} into a stream of {@link TermCode TermCodes}.
   *
   * @param concept the concept to expand
   * @return the stream of TermCodes
   */
  public Stream<ContextualTermCode> expandConcept(ContextualConcept concept) {
    List<ContextualTermCode> expandedCodes = conceptTree == null ? List.of() : expandCodes(concept);
    List<ContextualTermCode> concepts = expandedCodes.isEmpty() ? concept.contextualTermCodes() : expandedCodes;
    return concepts.stream().filter(mappings::containsKey);
  }

  private List<ContextualTermCode> expandCodes(ContextualConcept concept) {
    return concept.contextualTermCodes().stream().flatMap(conceptTree::expand).toList();
  }
}
