package com.empresa.fraude.adapter.in.web.validator;

import com.empresa.fraude.adapter.in.web.dto.TransaccionRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 * Validador Spring de nivel empresarial para TransaccionRequest.
 * Implementa la interfaz nativa org.springframework.validation.Validator de Spring Framework
 * para aplicar reglas de validación cruzada y coherencia de negocio.
 */
@Component
public class TransaccionRequestValidator implements Validator {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );

    @Override
    public boolean supports(Class<?> clazz) {
        return TransaccionRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target == null) {
            errors.reject("solicitud.nula", "El cuerpo de la solicitud no puede ser nulo");
            return;
        }

        TransaccionRequest request = (TransaccionRequest) target;

        validarCuentas(request, errors);
        validarTipoOperacion(request, errors);
        validarCoordenadas(request, errors);
        validarDireccionIp(request, errors);
    }

    private void validarCuentas(TransaccionRequest request, Errors errors) {
        String origen = request.getNumeroCuentaOrigen();
        String destino = request.getNumeroCuentaDestino();

        if (origen != null && destino != null) {
            if (origen.trim().equalsIgnoreCase(destino.trim())) {
                errors.rejectValue("numeroCuentaDestino", "CUENTAS_IDENTICAS",
                        "La cuenta de destino no puede ser idéntica a la cuenta de origen");
            }
        }
    }

    private void validarTipoOperacion(TransaccionRequest request, Errors errors) {
        if ("TRANSFERENCIA".equalsIgnoreCase(request.getTipoTransaccion())) {
            if (request.getNumeroCuentaDestino() == null || request.getNumeroCuentaDestino().trim().isEmpty()) {
                errors.rejectValue("numeroCuentaDestino", "CUENTA_DESTINO_REQUERIDA",
                        "Para transacciones de tipo TRANSFERENCIA, el número de cuenta de destino es obligatorio");
            }
        }
    }

    private void validarCoordenadas(TransaccionRequest request, Errors errors) {
        boolean tieneLatitud = request.getLatitud() != null;
        boolean tieneLongitud = request.getLongitud() != null;

        if (tieneLatitud != tieneLongitud) {
            errors.rejectValue("latitud", "COORDENADAS_INCOMPLETAS",
                    "Tanto latitud como longitud deben proporcionarse conjuntamente");
            return;
        }

        if (tieneLatitud) {
            double lat = request.getLatitud().doubleValue();
            double lon = request.getLongitud().doubleValue();

            if (lat < -90.0 || lat > 90.0) {
                errors.rejectValue("latitud", "LATITUD_INVALIDA",
                        "La latitud geográfica debe encontrarse entre -90.0 y 90.0");
            }

            if (lon < -180.0 || lon > 180.0) {
                errors.rejectValue("longitud", "LONGITUD_INVALIDA",
                        "La longitud geográfica debe encontrarse entre -180.0 y 180.0");
            }
        }
    }

    private void validarDireccionIp(TransaccionRequest request, Errors errors) {
        String ip = request.getDireccionIp();
        if (ip != null && !ip.trim().isEmpty()) {
            if (!IPV4_PATTERN.matcher(ip.trim()).matches()) {
                errors.rejectValue("direccionIp", "IP_INVALIDA",
                        "La dirección IP proporcionada no cumple con un formato IPv4 válido");
            }
        }
    }
}

