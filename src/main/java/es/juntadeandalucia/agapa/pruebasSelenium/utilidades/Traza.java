package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import lombok.extern.slf4j.Slf4j;
import org.testng.Reporter;


@Slf4j
public class Traza {

   /**
    * Escribe en consola a nivel INFO y en la seccion ReporterOutput del informe HTML
    */
   public static void info(String msg) {
      log.info(msg);
      Reporter.log(msg + System.lineSeparator());
   }

   /**
    * Escribe en consola a nivel ERROR y en la seccion ReporterOutput del informe HTML
    */
   public static void error(String msg) {
      log.info(msg);
      Reporter.log(msg + System.lineSeparator());
   }

}
