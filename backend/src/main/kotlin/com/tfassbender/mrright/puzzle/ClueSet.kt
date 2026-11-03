package com.tfassbender.mrright.puzzle

/**
 * The clues of a puzzle, split by kind: the generator needs to know which values each attribute clue allows
 * (finalists and near misses) and which names pass the name clues.
 *
 * - [attributeClues]: at most one per attribute; attributes without a clue allow every value.
 *   Their order is the order in which near misses are created – part of the generator.
 * - [ordered]: all clues in the order the player sees them.
 */
class ClueSet(
    attributes: List<AttributeDefinition>,
    val attributeClues: List<AttributeClue>,
    val nameClues: List<Clue>,
    val ordered: List<Clue>,
) {
    private val definitions: Map<String, AttributeDefinition> = attributes.associateBy { it.id }
    private val clueByAttribute: Map<String, AttributeClue> = attributeClues.associateBy { it.attribute }

    init {
        require(clueByAttribute.size == attributeClues.size) { "At most one clue per attribute: $attributeClues" }
        require(nameClues.none { it is AttributeClue }) { "Attribute clues listed as name clues: $nameClues" }
        require(ordered.size == attributeClues.size + nameClues.size && ordered.toSet() == (attributeClues + nameClues).toSet()) {
            "The ordered clues must be exactly the attribute and name clues"
        }
        attributeClues.forEach(::validate)
    }

    internal val nameGroups: NameGroups = NameGroups(nameClues)

    fun allowedValues(attribute: String): List<String> = valuesOf(attribute).filter { isAllowed(attribute, it) }

    fun disallowedValues(attribute: String): List<String> = valuesOf(attribute).filterNot { isAllowed(attribute, it) }

    private fun isAllowed(attribute: String, value: String): Boolean = clueByAttribute[attribute]?.allows(value) ?: true

    private fun valuesOf(attribute: String): List<String> = definitions.getValue(attribute).values

    /** A clue needs allowed values (for finalists) and disallowed ones (for its near misses). */
    private fun validate(clue: AttributeClue) {
        val definition = requireNotNull(definitions[clue.attribute]) { "Unknown attribute in $clue" }
        require(definition.values.containsAll(clue.referencedValues())) { "Unknown values in $clue" }
        require(allowedValues(clue.attribute).isNotEmpty()) { "$clue allows no value" }
        require(disallowedValues(clue.attribute).isNotEmpty()) { "$clue allows every value" }
    }
}
