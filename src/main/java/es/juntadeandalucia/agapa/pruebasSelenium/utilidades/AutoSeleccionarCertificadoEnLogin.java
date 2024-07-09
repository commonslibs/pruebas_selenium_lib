package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * La Interface AutoSeleccionarCertificadoEnLogin.
 *
 * @author AGAPA
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoSeleccionarCertificadoEnLogin {

   /**
    * Value.
    *
    * @return string
    */
   String value();
}
