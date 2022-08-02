package ooioo.ruches;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Pour calculs de distances entre les ruchers
 *    https://geoservices.ign.fr/documentation/services/api-et-services-ogc/itineraires/documentation-du-service-du-calcul
 *    distance en m et durée en minutes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Itineraire(float distance, float duration) {
}
