package es.juntadeandalucia.agapa.pruebasSelenium.utilidades.xpath;

/**
 * La Enum ConditionType.
 *
 * @author AGAPA
 */
public enum ConditionType {

   /** equals. */
   EQUALS("equals"),

   /** not equal. */
   NOT_EQUAL("not equal"),

   /** contains. */
   CONTAINS("contains"),

   /** not contain. */
   NOT_CONTAIN("not contain"),

   /** starts with. */
   STARTS_WITH("starts with"),

   /** ends with. */
   ENDS_WITH("ends with"),

   /** matches regex. */
   MATCHES_REGEX("matches regex"),

   /** not match regex. */
   NOT_MATCH_REGEX("not match regex"),

   /** expression. */
   // For mobile
   EXPRESSION("expression");

   /** text. */
   private String text;

   /**
    * Instancia un nuevo objeto de la clase condition type.
    *
    * @param text
    *           valor para: text
    */
   ConditionType(final String text) {
      this.text = text;
   }

   /**
    * To string.
    *
    * @return string
    */
   @Override
   public String toString() {
      return this.text;
   }

   /**
    * From value.
    *
    * @param value
    *           valor para: value
    * @return condition type
    */
   public static ConditionType fromValue(String value) {
      for (ConditionType condition : values()) {
         if (condition.toString().equals(value)) {
            return condition;
         }
      }

      return null;
   }
}
