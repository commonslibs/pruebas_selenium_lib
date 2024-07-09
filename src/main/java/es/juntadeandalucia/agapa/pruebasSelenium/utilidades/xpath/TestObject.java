package es.juntadeandalucia.agapa.pruebasSelenium.utilidades.xpath;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;


/**
 * Class TestObject.
 *
 * @author AGAPA
 */
public class TestObject {

   /** properties. */
   private List<TestObjectProperty> properties;

   /** object id. */
   private String                   objectId;

   /**
    * Instancia un nuevo objeto de la clase test object.
    *
    * @param objectId
    *           valor para: object id
    */
   public TestObject(String objectId) {
      this.properties = new ArrayList<>();
      this.objectId = objectId;
   }

   /**
    * Instancia un nuevo objeto de la clase test object.
    */
   public TestObject() {
      this(StringUtils.EMPTY);
   }

   /**
    * Get all properties of the test object.
    *
    * @return a list contains all properties of the test object
    */
   public List<TestObjectProperty> getProperties() {
      return this.properties;
   }

   /**
    * Get all active properties of the test object.
    *
    * @return a list contains all active properties of the test object
    */
   public List<TestObjectProperty> getActiveProperties() {
      List<TestObjectProperty> activeProperties = new ArrayList<>();
      for (TestObjectProperty property : this.properties) {
         if (property.isActive()) {
            activeProperties.add(property);
         }
      }
      return activeProperties;
   }

   /**
    * Set the properties of the test object.
    *
    * @param properties
    *           a list of properties to set to the test object
    */
   public void setProperties(List<TestObjectProperty> properties) {
      this.properties = properties;
   }

   /**
    * Add a new property to the test object.
    *
    * @param property
    *           the new {@link TestObjectProperty} to add
    * @return this test object after adding the new property
    */
   public TestObject addProperty(TestObjectProperty property) {
      this.properties.add(property);
      return this;
   }

   /**
    * Add a new property to the test object.
    *
    * @param name
    *           the name of the new property
    * @param condition
    *           the {@link ConditionType} of the new property
    * @param value
    *           the value of the new property
    * @return this test object after adding the new property
    */
   public TestObject addProperty(String name, ConditionType condition, String value) {
      this.properties.add(new TestObjectProperty(name, condition, value));
      return this;
   }

   /**
    * Find the value of a property using the property name.
    *
    * @param name
    *           the name of the property to find
    * @return the value of a property, or blank if not found
    */
   public String findPropertyValue(String name) {
      for (TestObjectProperty property : this.properties) {
         if (property.getName().equals(name)) {
            return property.getValue();
         }
      }
      return "";
   }

   /**
    * Find the value of a property using the property name.
    *
    * @param name
    *           the name of the property to find
    * @param caseSensitive
    *           boolean value to indicate if the finding process is case sensitive or not
    * @return the value of a property, or blank if not found
    */
   public String findPropertyValue(String name, boolean caseSensitive) {
      if (caseSensitive) {
         return this.findPropertyValue(name);
      }
      for (TestObjectProperty property : this.properties) {
         if (property.getName().equalsIgnoreCase(name)) {
            return property.getValue();
         }
      }
      return "";
   }

   /**
    * Find the property using the property name.
    *
    * @param name
    *           the name of the property to find
    * @return the found property, or null if not found
    */
   public TestObjectProperty findProperty(String name) {
      for (TestObjectProperty property : this.properties) {
         if (property.getName().equals(name)) {
            return property;
         }
      }
      return null;
   }

   /**
    * Get the id of this test object.
    *
    * @return the id of this test object
    */
   public String getObjectId() {
      return this.objectId;
   }

   /**
    * To string.
    *
    * @return string
    */
   @Override
   public String toString() {
      return "TestObject - '" + this.getObjectId() + "'";
   }

}
