package com.fireemu.domain.models

enum class RetroButton(val id: Int, val label: String) {
    B(0, "Button B"),
    Y(1, "Button Y"),
    SELECT(2, "Select"),
    START(3, "Start"),
    UP(4, "D-Pad Up"),
    DOWN(5, "D-Pad Down"),
    LEFT(6, "D-Pad Left"),
    RIGHT(7, "D-Pad Right"),
    A(8, "Button A"),
    X(9, "Button X"),
    L(10, "L Shoulder"),
    R(11, "R Shoulder"),
    L2(12, "L2 Trigger"),
    R2(13, "R2 Trigger"),
    L3(14, "L3 Thumb"),
    R3(15, "R3 Thumb")
}

data class KeyBinding(
    val button: RetroButton,
    val primaryKeyCode: Int,
    val customKeyCode: Int? = null
)
