package es.juntadeandalucia.agapa.pruebasSelenium.utilidades.robots;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory.Navegador;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
/**
 * Proporciona una utilidad para poder realizar la firma con autofirma de forma externa al navegador. Hay que tener en cuenta que cuando el
 * navegador invoca a la aplicación externa de Autofirma, la primera vez siempre muestra el diálogo para dar permiso a la aplicación, en
 * siguientes llamadas dentro de la misma sesión del webdriver no se muestra.
 *
 * @author dmartinez
 */
public class Autofirma {

   public static void firmarConAutofirma(Navegador navegador) throws PruebaAceptacionExcepcion {
      firmarConAutofirma(navegador, true);
   }

   public static void firmarConAutofirma(Navegador navegador, boolean primeraEjecucion) throws PruebaAceptacionExcepcion {
      if (Navegador.FIREFOX == navegador) {
         autoFirmaFirefox(primeraEjecucion);
      }
      else if (Navegador.CHROME == navegador || Navegador.MSEDGE == navegador) {
         autoFirmaChromium(primeraEjecucion);
      }
      else {
         throw new PruebaAceptacionExcepcion("No existe manejador para Autofirma en el navegador " + navegador.name());
      }
   }

   /**
    * Robot de autofirma específico para Chromium (Chrome y Edge)
    *
    * @throws PruebaAceptacionExcepcion
    */
   public static void autoFirmaChromium(boolean primeraEjecucion) throws PruebaAceptacionExcepcion {
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            log.info("Iniciando robot para firmar con Autofirma (Chromium)...");
            Robot robot = null;
            try {
               robot = new Robot();
               robot.delay(5000);
            }
            catch (AWTException e) {
               log.error("Error al instanciar el robot para firmar con autofirma (Chromium)", e);
            }
            // Pantalla de dialogo de chrome para seleccionar aplicación que trate Autofirma (siempre se muestra)
            if (primeraEjecucion) {
               robot.keyPress(KeyEvent.VK_LEFT);
               robot.keyRelease(KeyEvent.VK_LEFT);

               robot.keyPress(KeyEvent.VK_ENTER);
               robot.keyRelease(KeyEvent.VK_ENTER);

               robot.delay(5000);
            }
            // En este punto se muestra el dialogo de seleccion de certificado --> Intro del certificado
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            log.info("Terminado robot para firmar con Autofirma (Chromium)...");
            robot.delay(2000);
         }
      };

      new Thread(runnable).start();
   }

   /**
    * Robot de autofirma específico para Firefox
    *
    * @throws PruebaAceptacionExcepcion
    */
   public static void autoFirmaFirefox(boolean primeraEjecucion) throws PruebaAceptacionExcepcion {
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            log.info("Iniciando robot para firmar con Autofirma (Firefox)...");
            Robot robot = null;
            try {
               robot = new Robot();
               robot.delay(5000);
            }
            catch (AWTException e) {
               log.error("Error al instanciar el robot para firmar con autofirma (Firefox)", e);
            }
            // Pantalla de dialogo de firefox para seleccionar aplicación que trate Autofirma (siempre se muestra)
            if (primeraEjecucion) {
               for (int i = 0; i < 4; i++) {
                  robot.keyPress(KeyEvent.VK_TAB);
                  robot.keyRelease(KeyEvent.VK_TAB);
               }
               robot.keyPress(KeyEvent.VK_ENTER);
               robot.keyRelease(KeyEvent.VK_ENTER);

               robot.delay(5000);
            }
            // En este punto se muestra el dialogo de seleccion de certificado --> Intro del certificado
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            log.info("Terminado robot para firmar con Autofirma (Firefox)...");
            robot.delay(2000);
         }
      };

      new Thread(runnable).start();
   }

}
