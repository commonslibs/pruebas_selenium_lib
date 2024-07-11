package es.juntadeandalucia.agapa.pruebasSelenium.utilidades.xpath;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;


/**
 * Class XPathBuilder.
 *
 * @author AGAPA
 */
public class XPathBuilder {

   /**
    * Enum AggregationType.
    *
    * @author AGAPA
    */
   public enum AggregationType {

      /** union. */
      UNION,
      /** intersect. */
      INTERSECT
   }

   /** La constante XPATH_INTERSECT_FORMULA. */
   private static final String      XPATH_INTERSECT_FORMULA                   = "%s[count(. | %s) = count(%s)]";

   /** La constante XPATH_CONDITION_TYPE_NOT_MATCHES. */
   private static final String      XPATH_CONDITION_TYPE_NOT_MATCHES          = "not(matches(%s, '%s'))";

   /** La constante XPATH_CONDITION_TYPE_MATCHES. */
   private static final String      XPATH_CONDITION_TYPE_MATCHES              = "matches(%s, '%s')";

   /** La constante XPATH_CONDITION_TYPE_ENDS_WITH. */
   private static final String      XPATH_CONDITION_TYPE_ENDS_WITH            = "ends-with(%s, %s)";

   /** La constante XPATH_CONDITION_TYPE_STARTS_WITH. */
   private static final String      XPATH_CONDITION_TYPE_STARTS_WITH          = "starts-with(%s, %s)";

   /** La constante XPATH_CONDITION_TYPE_NOT_EQUALS. */
   private static final String      XPATH_CONDITION_TYPE_NOT_EQUALS           = "%s != %s";

   /** La constante XPATH_CONDITION_TYPE_NOT_CONTAINS. */
   private static final String      XPATH_CONDITION_TYPE_NOT_CONTAINS         = "not(contains(%s, %s))";

   /** La constante XPATH_CONDITION_TYPE_EQUALS. */
   private static final String      XPATH_CONDITION_TYPE_EQUALS               = "%s = %s";

   /** La constante XPATH_CONDITION_TYPE_CONTAINS. */
   private static final String      XPATH_CONDITION_TYPE_CONTAINS             = "contains(%s, %s)";

   /** La constante XPATH_TEXT_PROPERTY_EXPRESSION_FOR_WEBUI. */
   private static final String      XPATH_TEXT_PROPERTY_EXPRESSION_FOR_WEBUI  = "text()";

   /** La constante XPATH_TEXT_PROPERTY_EXPRESSION_FOR_MOBILE. */
   protected static final String    XPATH_TEXT_PROPERTY_EXPRESSION_FOR_MOBILE = "@text";

   /** tag. */
   private String                   tag;

   /** xpath. */
   private String                   xpath;

   /** predicates. */
   private List<String>             predicates;

   /** properties. */
   private List<TestObjectProperty> properties;

   /**
    * Instancia un nuevo objeto de la clase x path builder.
    *
    * @param properties
    *           valor para: properties
    */
   public XPathBuilder(List<TestObjectProperty> properties) {
      this.properties = properties;
   }

   /**
    * convenient function to avoid changing signature.
    *
    * @return string
    */
   public String build() {
      return this.build(AggregationType.INTERSECT);
   }

   /**
    * Union: "or" of all XPath locators, each contains a single condition.
    *
    * @param aggregationType
    *           valor para: aggregation type
    * @return string
    */
   public String build(AggregationType aggregationType) {

      boolean isIntersect = AggregationType.INTERSECT.equals(aggregationType);
      boolean isUnion = !isIntersect;

      boolean hasTagCondition = false;
      this.tag = "*";
      this.predicates = new ArrayList<>();
      this.xpath = StringUtils.EMPTY;

      if (this.properties != null && !this.properties.isEmpty()) {
         for (TestObjectProperty p : this.properties) {
            if (isUnion || p.isActive()) { // if union, use all properties
               String propertyName = p.getName();
               String propertyValue = p.getValue();
               ConditionType conditionType = p.getCondition();
               switch (PropertyType.nameOf(propertyName)) {
                  case ATTRIBUTE:
                     this.predicates.add(this.buildExpression("@" + propertyName, propertyValue, conditionType));
                     break;
                  case TAG:
                     hasTagCondition = true;
                     this.tag = propertyValue;
                     break;
                  case TEXT:
                     String textExpression = this.buildExpression(this.getXPathTextExpression(), propertyValue, conditionType);
                     String dotExpression = this.buildExpression(".", propertyValue, conditionType);
                     this.predicates.add(String.format("(%s or %s)", textExpression, dotExpression));
                     break;
                  case XPATH:
                     this.xpath = propertyValue;
                     break;
                  default:
                     break;
               }
            }
         }
      }

      List<String> xpaths = new ArrayList<>();

      if (StringUtils.isNotEmpty(this.xpath)) {
         xpaths.add(this.xpath);
      }

      if (!this.predicates.isEmpty()) {
         StringBuilder propertyBuilder = new StringBuilder();
         final String operator = isIntersect ? " and " : " or "; // intersect = and, union = or
         final String xpathTag = isIntersect ? this.tag : "*";
         propertyBuilder.append("//").append(xpathTag).append("[").append(StringUtils.join(this.predicates, operator)).append("]");
         xpaths.add(propertyBuilder.toString());
      }

      if (isUnion && hasTagCondition) { // union
         xpaths.add("//" + this.tag);
      }

      return this.combineXpathLocators(xpaths, aggregationType);
   }

   /**
    * Obtención del atributo: x path text expression.
    *
    * @return atributo: x path text expression
    */
   protected String getXPathTextExpression() {
      return XPATH_TEXT_PROPERTY_EXPRESSION_FOR_WEBUI;
   }

   /**
    * Builds the xpath based locators.
    *
    * @return list
    */
   public List<Entry<String, String>> buildXpathBasedLocators() {

      List<Entry<String, String>> locators = new ArrayList<>();

      if (this.properties != null && !this.properties.isEmpty()) {
         for (TestObjectProperty p : this.properties) {
            String propertyName = p.getName();
            String propertyValue = p.getValue();
            ConditionType conditionType = p.getCondition();
            Entry<String, String> entry = switch (PropertyType.nameOf(propertyName)) {
               case TEXT -> {
                  String textExpression = this.buildExpression("text()", propertyValue, conditionType);
                  String dotExpression = this.buildExpression(".", propertyValue, conditionType);
                  String predicate = String.format("(%s or %s)", textExpression, dotExpression);
                  String locator = "//*[" + predicate + "]";
                  yield new AbstractMap.SimpleEntry<>(propertyName, locator);
               }
               case XPATH -> new AbstractMap.SimpleEntry<>(propertyName, propertyValue);
               default -> null;
            };
            if (entry != null) {
               locators.add(entry);
            }
         }
      }

      return locators;
   }

   /**
    * Combine xpath locators.
    *
    * @param xpathList
    *           valor para: xpath list
    * @param aggregationType
    *           valor para: aggregation type
    * @return string
    */
   private String combineXpathLocators(List<String> xpathList, AggregationType aggregationType) {
      String xpathString;
      if (AggregationType.INTERSECT.equals(aggregationType)) {
         StringBuilder xpathStringBuilder = new StringBuilder();
         for (String xpath : xpathList) {
            if (xpathStringBuilder.toString().isEmpty()) {
               xpathStringBuilder.append(xpath);
            }
            else {
               String existingXpath = xpathStringBuilder.toString();
               xpathStringBuilder = new StringBuilder(String.format(XPATH_INTERSECT_FORMULA, existingXpath, xpath, xpath));
            }
         }
         xpathString = xpathStringBuilder.toString();
      }
      else {
         xpathString = StringUtils.join(xpathList, " | ");
      }
      return xpathString;
   }

   /**
    * Builds the expression.
    *
    * @param propertyName
    *           valor para: property name
    * @param propertyValue
    *           valor para: property value
    * @param contidionType
    *           valor para: contidion type
    * @return string
    */
   private String buildExpression(String propertyName, String propertyValue, ConditionType contidionType) {
      return switch (contidionType) {
         case CONTAINS -> String.format(XPATH_CONDITION_TYPE_CONTAINS, propertyName, this.escapeSingleQuote(propertyValue));
         case ENDS_WITH -> String.format(XPATH_CONDITION_TYPE_ENDS_WITH, propertyName, this.escapeSingleQuote(propertyValue));
         case EQUALS -> String.format(XPATH_CONDITION_TYPE_EQUALS, propertyName, this.escapeSingleQuote(propertyValue));
         case MATCHES_REGEX -> String.format(XPATH_CONDITION_TYPE_MATCHES, propertyName, propertyValue);
         case NOT_CONTAIN -> String.format(XPATH_CONDITION_TYPE_NOT_CONTAINS, propertyName, this.escapeSingleQuote(propertyValue));
         case NOT_EQUAL -> String.format(XPATH_CONDITION_TYPE_NOT_EQUALS, propertyName, this.escapeSingleQuote(propertyValue));
         case NOT_MATCH_REGEX -> String.format(XPATH_CONDITION_TYPE_NOT_MATCHES, propertyName, propertyValue);
         case STARTS_WITH -> String.format(XPATH_CONDITION_TYPE_STARTS_WITH, propertyName, this.escapeSingleQuote(propertyValue));
         default -> StringUtils.EMPTY;
      };
   }

   /**
    * Escape single quote.
    *
    * @param s
    *           valor para: s
    * @return string
    */
   private String escapeSingleQuote(String s) {
      if (!s.contains("'")) {
         return this.qoute(s);
      }

      String[] strings = s.split("'");

      StringBuilder xpathBuilder = new StringBuilder("concat(");
      for (int i = 0; i < strings.length; i++) {
         if (i > 0) {
            xpathBuilder.append(" , \"'\" , ");
         }

         xpathBuilder.append(this.qoute(strings[i]));
      }

      xpathBuilder.append(")");
      return xpathBuilder.toString();
   }

   /**
    * Qoute.
    *
    * @param s
    *           valor para: s
    * @return string
    */
   private String qoute(String s) {
      return "'" + s + "'";
   }

   /**
    * Enum PropertyType.
    *
    * @author AGAPA
    */
   public enum PropertyType {

      /** tag. */
      TAG,
      /** attribute. */
      ATTRIBUTE,
      /** text. */
      TEXT,
      /** xpath. */
      XPATH;

      /**
       * Name of.
       *
       * @param name
       *           valor para: name
       * @return property type
       */
      public static PropertyType nameOf(String name) {
         try {
            return valueOf(name.toUpperCase());
         }
         catch (IllegalArgumentException e) {
            return PropertyType.ATTRIBUTE;
         }
      }
   }
}
