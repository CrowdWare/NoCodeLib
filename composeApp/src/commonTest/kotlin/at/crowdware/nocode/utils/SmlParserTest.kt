package at.crowdware.nocode.utils

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

    @Test
    fun testGetIntValueDefaults() {
        // Create a SmlNode manually where the property is not an IntValue.
        val node = SmlNode("TestNode", mapOf("number" to PropertyValue.StringValue("notanumber")), emptyList())
        val intVal = getIntValue(node, "number", 100)
        assertEquals(100, intVal, "Default value should be returned for a non-integer property")
        
        // Test retrieval for a non-existent property.
        val missingInt = getIntValue(node, "missing", 50)
        assertEquals(50, missingInt, "Default value should be returned for a missing property")
    }

    @Test
    fun testGetFloatValueDefaults() {
        // Create a SmlNode manually where the property is not a FloatValue.
        val node = SmlNode("TestNode", mapOf("float" to PropertyValue.IntValue(42)), emptyList())
        val floatVal = getFloatValue(node, "float", 3.14f)
        assertEquals(3.14f, floatVal, "Default value should be returned for a non-float property")
        
        // Test retrieval for a non-existent property.
        val missingFloat = getFloatValue(node, "missing", 2.71f)
        assertEquals(2.71f, missingFloat, "Default value should be returned for a missing property")
    }

    @Test
    fun testGetStringValueDefaults() {
        // Create a SmlNode manually where the property is not a StringValue.
        val node = SmlNode("TestNode", mapOf("text" to PropertyValue.IntValue(123)), emptyList())
        val stringVal = getStringValue(node, "text", "default")
        assertEquals("default", stringVal, "Default value should be returned for a non-string property")
        
        // Test retrieval when property is correctly a StringValue.
        val node2 = SmlNode("TestNode", mapOf("greeting" to PropertyValue.StringValue("hello")), emptyList())
        val greeting = getStringValue(node2, "greeting", "default")
        assertEquals("hello", greeting, "Should retrieve the correct string value")
        
        // Test retrieval for a non-existent property.
        val missingString = getStringValue(node2, "missing", "absent")
        assertEquals("absent", missingString, "Default value should be returned for a missing property")
    }
    
    @Test
    fun testMultilineString() {
        val sml = """
            Root {
                text: "first line
second line
third line"
            }
        """.trimIndent()
        val (node, error) = parseSML(sml)
        assertNotNull(node, "Parsed node should not be null")
        assertNull(error, "Error should be null for valid multiline string SML")
        
        // Expected multiline string preserving line breaks.
        val expected = "first line\nsecond line\nthird line"
        val textVal = getStringValue(node!!, "text", "")
        assertEquals(expected, textVal, "The multiline text property should preserve line breaks")
    }
    
    @Test
    fun testLinkStringWithColon() {
        val sml = """
            Root {
                link: "web:https://example.com"
            }
        """.trimIndent()
        val (node, error) = parseSML(sml)
        assertNotNull(node, "Parsed node should not be null")
        assertNull(error, "Error should be null for valid SML with link")
        
        val linkVal = getStringValue(node!!, "link", "")
        assertEquals("web:https://example.com", linkVal, "The link property should be parsed correctly")
    }
}
