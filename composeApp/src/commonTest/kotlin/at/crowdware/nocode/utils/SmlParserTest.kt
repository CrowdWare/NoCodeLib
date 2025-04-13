package at.crowdware.nocode.utils
/* 
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SmlParserTest {

    @Test
    fun testParseValidSml() {
        val sml = """
            Root {
                enabled: true
                value: 42
                text: "hello"
            }
        """.trimIndent()
        val (node, error) = parseSML(sml)
        assertNotNull(node, "Parsed node should not be null")
        assertNull(error, "Error should be null for valid SML")
        
        // Verify getBoolValue, getIntValue, and getStringValue on the parsed node.
        val boolVal = getBoolValue(node!!, "enabled", false)
        assertTrue(boolVal, "The 'enabled' property should be true")
        
        val intVal = getIntValue(node, "value", 0)
        assertEquals(42, intVal, "The 'value' property should be 42")
        
        val textVal = getStringValue(node, "text", "")
        assertEquals("hello", textVal, "The 'text' property should be 'hello'")
    }
    
    @Test
    fun testParseInvalidSml() {
        // This SML string is invalid due to a missing closing brace.
        val sml = "Root { enabled: true"
        val (node, error) = parseSML(sml)
        assertNull(node, "Parsed node should be null for invalid SML")
        assertNotNull(error, "An error message should be returned for invalid SML")
    }
    
    @Test
    fun testGetBoolValueDefaults() {
        // Create a SmlNode manually where the property is not a BooleanValue.
        val node = SmlNode("TestNode", mapOf("flag" to PropertyValue.IntValue(0)), emptyList())
        // Since 'flag' is not a BooleanValue, getBoolValue should return the default.
        val boolVal = getBoolValue(node, "flag", true)
        assertTrue(boolVal, "Default value should be returned for a non-boolean property")
        
        // Test retrieval for a non-existent property.
        val missingBool = getBoolValue(node, "nonexistent", false)
        assertFalse(missingBool, "Default value should be returned for a missing property")
    }
}
*/