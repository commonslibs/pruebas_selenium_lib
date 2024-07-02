package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;


@Slf4j
public class UtilidadesFicheros {

   /**
    * Obtiene un fichero de la carpeta resource.
    *
    * @param fichero
    *           valor para: fichero
    * @return File o null si no lo encentra.
    */
   public static File getFicheroResource(String fichero) throws PruebaAceptacionExcepcion {
      return new File(UtilidadesFicheros.getRutaDelResource(fichero));
   }

   public static String getRutaDelResource(String ruta) throws PruebaAceptacionExcepcion {
      ClassLoader classLoader = UtilidadesFicheros.class.getClassLoader();
      URL url = classLoader.getResource(ruta);
      if (url != null) {
         return new File(url.getFile()).getPath();
      }
      throw new PruebaAceptacionExcepcion("En el RESOURCE, no se encuentra la ruta: " + ruta);
   }

   /**
    * Primero verifica que el fichero existe en la ruta del resource, ya que si aqui no existe, tampoco estará en el Docker. Luego verifica
    * el estado de la variable docker para saber si los test se estan lanzando en Docker's, en este caso (como se ha validado que existe en
    * el resource) se modifica la ruta del fichero para que se encuentre en el Docker. Si no es el caso, se devuelve la ruta verificada del
    * fichero del resource.
    *
    * @param fichero
    *           valor para: fichero
    * @return File o null si no lo encentra.
    * @throws PruebaAceptacionExcepcion
    */
   // TODO: Quizás sobre y se pueda usar getRutaDelResource. En ese caso también sobraría RUTA_FICHEROS_DE_PRUEBAS_EN_EL_DOCKER
   public static String getRutaInDockerOrResource(String fichero) throws PruebaAceptacionExcepcion {

      // FIXME: Podría fallar, revisar
      String ruta = UtilidadesFicheros.getRutaDelResource(fichero);
      // if (VariablesGlobalesTest.IS_DOCKER) {
      // ruta = VariablesGlobalesTest.getPropiedad(Propiedades.RUTA_FICHEROS_DE_PRUEBAS_EN_EL_DOCKER);
      // return ruta + "/" + fichero;
      // }
      // else {
      return new File(ruta).getPath();
      // }
   }

   public static String cargarFicheroResource(String fichero) throws PruebaAceptacionExcepcion {
      File file = UtilidadesFicheros.getFicheroResource(fichero);
      String data;
      try {
         data = FileUtils.readFileToString(file, "UTF-8");
      }
      catch (IOException e) {
         UtilidadesFicheros.log.error("Error al leer el fichero: " + fichero, e);
         throw new PruebaAceptacionExcepcion(e.getMessage());
      }
      Assert.assertNotNull(data);
      return data;
   }

}
