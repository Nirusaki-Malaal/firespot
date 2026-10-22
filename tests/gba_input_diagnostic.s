/* CC0-1.0. Original GBA mode-3 input colour and PSG audio diagnostic. */
.syntax unified
.cpu arm7tdmi
.arm
.global _start
_start:
 b start
 .org 0xc0
start:
 ldr r0, =0x04000000
 ldr r1, =0x0403
 strh r1, [r0]
 mov r1, #0x80
 strh r1, [r0, #0x84]
 ldr r1, =0x1177
 strh r1, [r0, #0x80]
 mov r1, #2
 strh r1, [r0, #0x82]
 ldr r1, =0xf080
 strh r1, [r0, #0x62]
 ldr r1, =0x8700
 strh r1, [r0, #0x64]
 ldr r2, =0x06000000
 ldr r4, =0x04000130
loop:
 ldrh r3, [r4]
 tst r3, #0x10
 moveq r3, #31
 movne r3, #0x7c00
 ldr r5, =38400
 mov r6, r2
fill:
 strh r3, [r6], #2
 subs r5, r5, #1
 bne fill
 b loop
 .ltorg
 .org 32768
