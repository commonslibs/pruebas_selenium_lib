package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import java.io.InputStream;
import java.util.Properties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * Clase que reocge el valor de las varaible del property
 *
 * @author ISOTROL
 */
@Data
@Slf4j
public class VariablesGlobalesTest {

   private static Properties propiedades = null;

   /**
    * Contiene el nombre de la variable del property por el que busca
    *
    * @author ISOTROL
    */
   public enum PropiedadesTest implements PropiedadGenerica {
      NAVEGADOR, MAXIMIZAR, TIEMPO_RETRASO_CORTO, TIEMPO_RETRASO_MEDIO, TIEMPO_RETRASO_LARGO, TIEMPO_RETRASO_AUTOFIRMA,
      ID_ELEMENTO_PROCESANDO, POSICION_CERTIFICADO
   }

   /**
    * Recoge el valor del porperty filtrando por el entorno de desarollo, por defecto recoge IC
    *
    * @param propiedad
    * @return Valor de las propiedades del property
    * @throws PruebaAceptacionExcepcion
    */
   public static String getPropiedad(PropiedadGenerica propiedad) throws PruebaAceptacionExcepcion {
      if (propiedades == null) {
         String entorno = System.getProperty("entorno");

         if (entorno != null && !"".equals(entorno)) {
            propiedades = getFilePathToSaveStatic("application-" + entorno + ".properties");
         }
      }

      String nombrePropiedad = propiedad.toString();
      if (!propiedades.containsKey(nombrePropiedad)) {
         throw new PruebaAceptacionExcepcion("Parámetro " + propiedad + " no se ha encontrado en el fichero de configuración");
      }

      if (StringUtils.isBlank(propiedades.get(nombrePropiedad).toString())) {
         throw new PruebaAceptacionExcepcion("Parámetro " + propiedad + " de configuración inválido");
      }
      return propiedades.get(nombrePropiedad).toString();
   }

   /**
    * Saca los valores del archivo.
    *
    * @param fileName
    * @return Valores del archivo.
    */
   private static Properties getFilePathToSaveStatic(String fileName) {

      Properties prop = new Properties();
      try (InputStream inputStream = VariablesGlobalesTest.class.getClassLoader().getResourceAsStream(fileName)) {
         prop.load(inputStream);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }

      return prop;
   }

}