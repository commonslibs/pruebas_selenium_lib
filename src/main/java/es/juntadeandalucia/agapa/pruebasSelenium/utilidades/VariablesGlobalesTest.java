package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * Clase que reocge el valor de las varaible del property.
 *
 * @author AGAPA
 */
@Slf4j
public class VariablesGlobalesTest {

   /** La constante IS_DOCKER. */
   public static final boolean IS_DOCKER                          =
         Boolean.parseBoolean(System.getProperty("docker", Boolean.FALSE.toString()).toLowerCase());

   /** La constante IS_VIDEO_GRABAR. */
   public static final boolean IS_VIDEO_GRABAR                    =
         Boolean.parseBoolean(System.getProperty("video.grabar", Boolean.FALSE.toString()).toLowerCase());

   /** La constante IS_VIDEO_GRABAR_TODOS. */
   public static final boolean IS_VIDEO_GRABAR_TODOS              =
         Boolean.parseBoolean(System.getProperty("video.grabar.todos", Boolean.FALSE.toString()).toLowerCase());

   /** La constante IS_MODO_INCOGNITO. */
   public static final boolean IS_MODO_INCOGNITO                  =
         Boolean.parseBoolean(System.getProperty("modoIncognito", Boolean.FALSE.toString()).toLowerCase());

   /** La constante IS_HEADLESS. */
   public static final boolean IS_HEADLESS                        =
         Boolean.parseBoolean(System.getProperty("java.awt.headless", Boolean.FALSE.toString()).toLowerCase());

   /** La constante BORRAR_CACHE. */
   public static final boolean BORRAR_CACHE                       =
         Boolean.parseBoolean(System.getProperty("borrar.cache", Boolean.FALSE.toString()).toLowerCase());

   /** La constante HTTP_DOCKER. */
   public static final String  HTTP_DOCKER                        = System.getProperty("docker.nodeGridUrl", "http://selenium-hub:4444");

   /** La constante DIRECTORIO_TARGET_SUREFIRE_REPORTS. */
   public static final String  DIRECTORIO_TARGET_SUREFIRE_REPORTS =
         System.getProperty("user.dir") + File.separator + "target" + File.separator + "surefire-reports" + File.separator;

   /** La constante DIRECTORIO_CAPTURAS. */
   public static final String  DIRECTORIO_CAPTURAS                = "capturas" + File.separator;

   /** propiedades. */
   private static Properties   propiedades                        = null;

   /**
    * Contiene el nombre de la variable del property por el que busca.
    *
    * @author AGAPA
    */
   public enum PropiedadesTest implements PropiedadGenerica {

      /** navegador. */
      NAVEGADOR,
      /** maximizar. */
      MAXIMIZAR,
      /** tiempo retraso corto. */
      TIEMPO_RETRASO_CORTO,
      /** tiempo retraso medio. */
      TIEMPO_RETRASO_MEDIO,
      /** tiempo retraso largo. */
      TIEMPO_RETRASO_LARGO,
      /** tiempo retraso autofirma. */
      TIEMPO_RETRASO_AUTOFIRMA,

      /** id elemento procesando. */
      ID_ELEMENTO_PROCESANDO,
      /** posicion certificado. */
      POSICION_CERTIFICADO,
      /** https proxy. */
      HTTPS_PROXY,
      /** milisegundos espera obligatoria. */
      MILISEGUNDOS_ESPERA_OBLIGATORIA
   }

   /**
    * Recoge el valor del porperty filtrando por el entorno en el que se ejecuta.
    *
    * @param propiedad
    *           valor para: propiedad
    * @return Valor de las propiedades del property
    * @throws IllegalArgumentException
    *            la illegal argument exception
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
            VariablesGlobalesTest.propiedades = VariablesGlobalesTest.getFilePathToSaveStatic("application-" + entorno + ".properties");
         }
      }

      String nombrePropiedad = propiedad.toString();
      if (!VariablesGlobalesTest.propiedades.containsKey(nombrePropiedad)) {
         throw new IllegalArgumentException("Parámetro " + propiedad + " no se ha encontrado en el fichero de configuración");
      }

      if (StringUtils.isBlank(VariablesGlobalesTest.propiedades.get(nombrePropiedad).toString())) {
         throw new IllegalArgumentException("Parámetro " + propiedad + " de configuración inválido");
      }
      return VariablesGlobalesTest.propiedades.get(nombrePropiedad).toString();
   }

   /**
    * Recoge el valor del porperty filtrando por el entorno en el que se ejecuta.
    *
    * @param propiedad
    *           valor para: propiedad
    * @param valorPorDefecto
    *           valor para: valor por defecto
    * @return Valor de las propiedades del property
    * @throws IllegalArgumentException
    *            la illegal argument exception
    */
   public static String getPropiedadOpcional(PropiedadGenerica propiedad, String valorPorDefecto) throws IllegalArgumentException {
      String valorPropiedad = null;
      try {
         valorPropiedad = VariablesGlobalesTest.getPropiedad(propiedad);
      }
      catch (IllegalArgumentException e) {
         valorPropiedad = valorPorDefecto;
         VariablesGlobalesTest.log.info("Propiedad " + propiedad.toString() + " no existe. Se usa el valor por defecto=" + valorPorDefecto);
      }
      return valorPropiedad;
   }

   /**
    * Saca los valores del archivo.
    *
    * @param fileName
    *           valor para: file name
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