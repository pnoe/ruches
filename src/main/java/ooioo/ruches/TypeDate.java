package ooioo.ruches;

import java.time.LocalDateTime;

import ooioo.ruches.evenement.TypeEvenement;

public record TypeDate(TypeEvenement type, LocalDateTime date) {
}
