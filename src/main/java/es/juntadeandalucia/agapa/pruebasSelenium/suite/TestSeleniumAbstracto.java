package es.juntadeandalucia.agapa.pruebasSelenium.suite;

import static org.testng.Assert.assertNotNull;

import com.automation.remarks.testng.UniversalVideoListener;
import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.reports.InformeListener;
import es.juntadeandalucia.agapa.pruebasSelenium.reports.ResumenListener;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.Traza;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory.Navegador;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Listeners;


/**
 * Añade por defecto listeners
 */
@Listeners({ ResumenListener.class, InformeListener.class, UniversalVideoListener.class })
public abstract class TestSeleniumAbstracto extends AbstractTestNGSpringContextTests {

   /** Instacia de webdriver usado en el caso de prueba */
   @Getter
   protected WebDriver driver;

   /**
    * Metodo que se ejecuta antes de los test.
    *
    * @throws PruebaAceptacionExcepcion
    */
   public void beforeMethod() throws PruebaAceptacionExcepcion {
      try {
         // Indicara la carpeta donde se guardaran los videos.
         System.setProperty("video.folder",
               System.getProperty("user.dir") + "//target//surefire-reports//video//" + this.getClass().getSimpleName());

         this.iniciar();
      }
      catch (Exception e) {
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
   }

   public void afterMethod() throws PruebaAceptacionExcepcion {
      this.driver.quit();
   }

   private void iniciar() throws PruebaAceptacionExcepcion {
      Navegador navegador = Navegador.valueOf(VariablesGlobalesTest.getPropiedad(PropiedadesTest.NAVEGADOR));
      this.driver = WebDriverFactory.obtenerInstancia(navegador);
      ChromeDriver chrome = (ChromeDriver) this.driver;
      Traza.info(chrome.toString());
      assertNotNull(this.driver, "Error al instanciar el driver de " + navegador);
      String propiedadMaximizar = null;
      try {
         propiedadMaximizar = VariablesGlobalesTest.getPropiedad(PropiedadesTest.MAXIMIZAR);
      }
      catch (IllegalArgumentException e) {
         Traza.info("Propiedad MAXIMIZAR no definida. Se asume como TRUE");
      }
      boolean maximizar;
      if (StringUtils.isEmpty(propiedadMaximizar)) {
         maximizar = true;
      }
      else {
         maximizar = Boolean.valueOf(propiedadMaximizar);
      }
      if (maximizar) {
         this.driver.manage().window().maximize();
      }
   }

}
