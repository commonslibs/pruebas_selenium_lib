package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import lombok.extern.slf4j.Slf4j;


/**
 * Clase que reocge el valor de las varaible del property
 *
 * @author AGAPA
 */
@Slf4j
public class VariablesGlobalesTest {

   public static final boolean IS_DOCKER                          =
         Boolean.parseBoolean(System.getProperty("docker", Boolean.FALSE.toString()).toLowerCase());

   public static final boolean IS_VIDEO_GRABAR                    =
         Boolean.parseBoolean(System.getProperty("video.grabar", Boolean.FALSE.toString()).toLowerCase());

   public static final boolean IS_VIDEO_GRABAR_TODOS              =
         Boolean.parseBoolean(System.getProperty("video.grabar.todos", Boolean.FALSE.toString()).toLowerCase());

   public static final boolean IS_MODO_INCOGNITO                  =
         Boolean.parseBoolean(System.getProperty("modoIncognito", Boolean.FALSE.toString()).toLowerCase());

   public static final boolean IS_HEADLESS                        =
         Boolean.parseBoolean(System.getProperty("java.awt.headless", Boolean.FALSE.toString()).toLowerCase());

   public static final String  HTTP_DOCKER                        = "http://selenium-hub:4444";

   public static final String  DIRECTORIO_TARGET_SUREFIRE_REPORTS = System.getProperty("user.dir") + File.separator
         + "target" + File.separator + "surefire-reports" + File.separator;

   public static final String  DIRECTORIO_CAPTURAS                = "capturas" + File.separator;

   private static Properties   propiedades                        = null;

   /**
    * Contiene el nombre de la variable del property por el que busca
    */
   public enum PropiedadesTest implements PropiedadGenerica {
      NAVEGADOR, MAXIMIZAR, TIEMPO_RETRASO_CORTO, TIEMPO_RETRASO_MEDIO, TIEMPO_RETRASO_LARGO, TIEMPO_RETRASO_AUTOFIRMA,
      ID_ELEMENTO_PROCESANDO, POSICION_CERTIFICADO, HTTPS_PROXY, MILISEGUNDOS_ESPERA_OBLIGATORIA
   }

   /**
    * Recoge el valor del porperty filtrando por el entorno en el que se ejecuta
    *
    * @param propiedad
    * @return Valor de las propiedades del property
    * @throws PruebaAceptacionExcepcion
    */
   public static String getPropiedad(PropiedadGenerica propiedad) throws IllegalArgumentException {
      if (VariablesGlobalesTest.propiedades == null) {
         String entorno = System.getProperty("entorno");

         if ("".equals(entorno)) {
            String mensaje = "Perfil no definido";
            Traza.error(mensaje);
            throw new IllegalArgumentException(mensaje);
         }

         if (entorno != null && !"".equals(entorno)) {
            VariablesGlobalesTest.propiedades =
                  VariablesGlobalesTest.getFilePathToSaveStatic("application-" + entorno + ".properties");
         }
      }

      String nombrePropiedad = propiedad.toString();
      if (!VariablesGlobalesTest.propiedades.containsKey(nombrePropiedad)) {
         throw new IllegalArgumentException(
               "Parámetro " + propiedad + " no se ha encontrado en el fichero de configuración");
      }

      if (StringUtils.isBlank(VariablesGlobalesTest.propiedades.get(nombrePropiedad).toString())) {
         throw new IllegalArgumentException("Parámetro " + propiedad + " de configuración inválido");
      }
      return VariablesGlobalesTest.propiedades.get(nombrePropiedad).toString();
   }

   /**
    * Recoge el valor del porperty filtrando por el entorno en el que se ejecuta
    *
    * @param propiedad
    * @return Valor de las propiedades del property
    * @throws PruebaAceptacionExcepcion
    */
   public static String getPropiedadOpcional(PropiedadGenerica propiedad, String valorPorDefecto)
         throws IllegalArgumentException {
      String valorPropiedad = null;
      try {
         valorPropiedad = VariablesGlobalesTest.getPropiedad(propiedad);
      }
      catch (IllegalArgumentException e) {
         valorPropiedad = valorPorDefecto;
         VariablesGlobalesTest.log.info(
               "Propiedad " + propiedad.toString() + " no existe. Se usa el valor por defecto=" + valorPorDefecto);
      }
      return valorPropiedad;
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
         VariablesGlobalesTest.log.error(e.getLocalizedMessage());
      }

      return prop;
   }

}