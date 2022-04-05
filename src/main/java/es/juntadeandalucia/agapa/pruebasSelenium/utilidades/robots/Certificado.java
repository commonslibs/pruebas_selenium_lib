package es.juntadeandalucia.agapa.pruebasSelenium.utilidades.robots;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory.Navegador;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
/**
 * Proporciona una utilidad para poder seleccionar un certificado digital. Es importante que durante el proceso, el navegador de pruebas se
 * mantenga en primer plano, de lo contrario el robot ejecutará los comandos sobre la aplicación que se encuentre en primer plano
 */
public class Certificado {

   /**
    * Método que lanza un proceso en segundo plano para seleccionar un certificado digial IMPORTANTE: Hay que llamar esta función justo
    * antes de que se abra la ventana de selección de certificados
    *
    * @param navegador
    *           El navegador en el que está corriendo selenium
    * @param posicionCertificado
    *           La posición donde se encuentra el certificado, siendo 1 la primera posición
    */
   public static void seleccionarCertificado(Navegador navegador, int posicionCertificado) throws PruebaAceptacionExcepcion {
      if (Navegador.FIREFOX == navegador) {
         // TODO - Implementar la selección de certificado con firefox
      }
      else if (Navegador.CHROME == navegador) {
         seleccionarCertificadoChrome(posicionCertificado);
      }
      else {
         throw new PruebaAceptacionExcepcion(
               "No hay implementado un robot para seleccionar el certificado digital en el navegador " + navegador.name());
      }
   }

   public static void seleccionarCertificadoChrome(int posicionCertificado) throws PruebaAceptacionExcepcion {
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            log.info("Iniciando selección de certificado digital (chrome)...");
            Robot robot = null;
            try {
               robot = new Robot();
               robot.delay(1500);

               for (int i = 1; i < posicionCertificado; i++) {
                  robot.keyPress(KeyEvent.VK_DOWN);
                  robot.keyRelease(KeyEvent.VK_DOWN);
                  robot.delay(300);
               }

               robot.delay(300);
               robot.keyPress(KeyEvent.VK_ENTER);
               robot.keyRelease(KeyEvent.VK_ENTER);

            }
            catch (AWTException e) {
               log.error("Error al seleccionar el certificado digital con robots (chrome)", e);
            }

         }
      };

      new Thread(runnable).start();
   }

}
