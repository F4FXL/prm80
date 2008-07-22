;    Copyright (c) 2007, 2008 Florian MAZEN and Pierre COL
;    
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

;----------------------------------------
; Chargement du verrou serie
; Avec un call : duree 196us
;----------------------------------------
load_serial_latch:
	mov	a, serial_latch_hi 
	call	lsc_send	; Transfert du premier octet
	mov	a, serial_latch_lo
	call	lsc_send	; Transfert du deuxieme octet
	clr	ser_scl		; Horloge serie a l'etat bas
	setb	latch_str	; Transfert donnee
	clr	latch_oe	; activer sortie
	clr	latch_str	; fin transfert
	ret

; Sous routine d'envoi de l'octet dans A pour le verrou
lsc_send:
	call	wdt_reset
	mov	r2, #8		; 8 boucles dans le compteur
lsc_send_lp:
	clr	ser_scl		; Horloge serie a l'etat bas
	mov	c, acc.7	; Copie de l'octet a transferer dans C
	mov	ser_sda, c	; puis recopie sur le port
	nop
	nop
	setb	ser_scl		; Generer un front montant
	rl	a		; Preparer bit suivant
	djnz	r2,lsc_send_lp	; fin de la boucle
	ret

;----------------------------------------
; Watchdog
; Avec un call : duree 8us
;----------------------------------------
wdt_reset:
	orl	pcon, #10h
	mov	T3, #wdt_int
	ret

;----------------------------------------
; Volume
;----------------------------------------
set_volume:
	jb	lock.2, sv_end		; Si volume verroulle alors fin

	mov	a, #00h			; Effacer les flags
	mov	adcon, a

	mov	a, #07			; Lire port 7
	mov	adcon, a

	mov	a, #0fh			; Lancer la capture
	mov	adcon, a

sv_adc_lp:
	call	wdt_reset
	mov	a, adcon	
	jnb	acc.4, sv_adc_lp	; Attendre la fin de lecture de l'adc

	; Modification du volume
	mov	a, adch
	cpl	a
	cjne	a, vol_hold, sv_cont1	; Test la valeur memoris� et la valeur courante
	ret				; fin si egaux

load_volume:
sv_cont1:
	cjne	a, #0ffh, sv_unmute
	mov	vol_hold, a
	setb	P4.1			; mute
	jmp	sv_end
sv_unmute:
	clr	P4.1
	anl	serial_latch_hi, #0f0h
	mov	vol_hold, a
	mov	a, #0
	mov	c, vol_hold.7
	mov	acc.0, c
	mov	c, vol_hold.6
	mov	acc.1, c
	mov	c, vol_hold.5
	mov	acc.2, c	
	mov	c, vol_hold.4
	mov	acc.3, c	
	orl	serial_latch_hi, a
	call	load_serial_latch
	

sv_end:
	call	wdt_reset
	mov	a, #00h			; Effacer les flags
	mov	adcon, a

	ret

;----------------------------------------
; Chargement dans le synthetiseur
;----------------------------------------
; Division par N a charger dans r0 (lsb), r1 (msb)
; r0 contien 6bit pour A. Donc pour M, 8 bits dans r1 et 2 bits dans r0
load_synth_reg:
	call	wdt_reset
	setb	ser_scl
	setb	synth_ce
	
	;Chargement de A (7bits), bit 6 tjs � 0
	mov	a, r0
	rl	a
	clr	acc.7
	mov	r2, #07h
	call	ls_send
	
	;Chargement de M (10bits)
	mov	a, r1		; Envoi d'abord les 8 bits de poids fort
	mov	r2, #08h
	call	ls_send
	mov	a, r0		; Puis les 2 bits de poids faible
	mov	r2, #02h
	call	ls_send

	;Chargement de R (11bits)
	mov	dph, #ram_area_config
	mov	dpl, #ram_pll_div_hi
	movx	a, @dptr		; Envoi d'abord les 3 bits de poids fort
	mov	r2, #03
	call	ls_send
	mov	dpl, #ram_pll_div_lo
	movx	a, @dptr		; Puis les 8 bits de poids faible
	mov	r2, #08h
	call	ls_send

	clr	synth_ce
	ret


load_synth:
	mov	r4, #10
ls_lp:
	call	load_synth_reg
	mov	r5, #15
ls_lp1:
	mov	r6, #0ffh
ls_lp0:
	call	wdt_reset
	djnz	r6, ls_lp0
	djnz	r5, ls_lp1	
	mov	a, P5
	jnb	acc.2, ls_end
	djnz	r4, ls_lp
	jmp	ls_error

ls_end:
	setb	mode.4
	ret
	
ls_error:
	clr	mode.4
	mov	r0, #0eeh
	call	lcd_clear_digits_r
	call	lcd_print_hex
	call	load_lcd
lse_lp:
	call	wdt_reset
	call	TERMINAL
	jmp	lse_lp


; Enoie d'une serie de bit, r2 contien le nombre et a les donn�es	
ls_send:
	call	wdt_reset
	setb	ser_scl		; Horloge serie a l'etat haut
	mov	c, acc.7	; Copie de l'octet a transferer dans C
	mov	ser_sda, c	; puis recopie sur le port
	nop
	nop
	clr	ser_scl		; Generer un front descendant
	rl	a		; Preparer bit suivant
	djnz	r2,ls_send	; fin de la boucle
	ret

;----------------------------------------
; Test si squelch ouvert
;----------------------------------------
squelch:
	mov	a, P5
	rl	a
	
	; Verroullage de la RX
	mov	c, lock.3
	cpl	c
	anl	c, Acc.2
	mov	Acc.2, c	

	xrl	a, mode
	jb	acc.2, sql_cont
	ret
sql_cont:	
	cpl	mode.2
	
	mov	a, serial_latch_hi
	cpl	acc.4
	mov	serial_latch_hi, a
	call	load_serial_latch
	ret

;----------------------------------------
; Commutation de la puissance
;----------------------------------------
switch_power:
	mov	a, #0feh
	anl	serial_latch_lo, a
	cpl	mode.1
	call	save_mode
	mov	a, mode
	rr	a
	anl	a, #01h
	orl	serial_latch_lo, a
	call	load_serial_latch	
	ret

;----------------------------------------
; Chargement de la puissance depuis
; l'etat de "mode"
;----------------------------------------
load_power:
	mov	a, #0feh
	anl	serial_latch_lo, a
	mov	a, mode
	rr	a
	anl	a, #01h
	orl	serial_latch_lo, a
	call	load_serial_latch	
	ret

;----------------------------------------
; 1750
;----------------------------------------
	
; "Check1750" :  tant que l'appui sur le BP "1750" (= ILS du micro)
;                et le PTT sont presents simultement, generer du
;                1750 Hz sur la sortie alarme (bit B2 du LATCH_LSB),
;                c'est a dire la complementer toutes les 286 us. 
;                P4.0 = entree PTT, active sur un "0". 
;                P5.5 = entree etat ILS du micro, active sur un "0".

check1750:	
	call	wdt_reset
                         ; ### debut de la boucle ###
test_ptt_ils:    
	JB       P4.0,fin1750     	; Si PTT relache (a "1"), ou
	call	 check_button_1750	; Bouton 1750 micro relache (a "1")ou
	mov	 A, P5			; Bouton 1750 facade relache, fin
	anl	 c, acc.5
	jb	 psw.7, fin1750		; de la routine ; sinon : 
        CPL      serial_latch_lo.2    	; complementer la sortie 
        LCALL    load_serial_latch	; alarme, puis attendre 
        MOV      R0,#29			; suffisamment pour que la
tempo1750:
	DJNZ     R0,tempo1750     	; boucle totale dure 286 us 
;	NOP                         	; (sinon il manquerait 134 us),
        JMP      test_ptt_ils    	; et enfin reboucler.
        		 ; ### fin de la boucle ###;
fin1750:        
	CLR        serial_latch_lo.2      	; Basculer la sortie alarme a 0.
	RET                         	; Fin de la routine...

;----------------------------------------
; "Tempo2ms" : realise une temporisation de 2 ms :
;   T = 5+(10.N)+5 = 10.(N+1) us ; avec N=199, T=2000us=2ms.
;----------------------------------------

Tempo2ms:                                 ;    2 - Le "CALL" de la routine.
                 PUSH       0             ;    2 - Sauvegarde R0 sur la pile.
                 MOV        R0,#199       ;    1 - Nombre de boucles : 199.
tmp_bcl:         	                  ;  ;;  1 Dur�e de la boucle : 10�s.
		 call	    wdt_reset
                 DJNZ       R0,tmp_bcl    ;  ;;  2
                 NOP                      ; 1 - Attendre une �s.
                 POP        0             ; 2 - On r�cup�re R0 sur la pile.
                 RET                      ; 2   Fin de routine...

;----------------------------------------
; "Tempo50ms" : Realise une temporisation
;   de 50 ms environ (peu critique).
;----------------------------------------
Tempo50ms:       PUSH       1             ; Sauvegarde R1 sur la pile.
                 MOV        R1,#25        ; Nombre de boucles : 25.
tmp_bcl50:       LCALL      Tempo2ms      ; 25 x 2ms = 50 ms.
                 DJNZ       R1,tmp_bcl50  ; 
                 POP        1             ; On r�cup�re R1 sur la pile.
                 RET                      ; Fin de routine...

;----------------------------------------
; Bip 200ms
;----------------------------------------
bip:
	push	0
	mov	pwm1, #127
	mov	r0, #4
bip_loop:
	call	Tempo50ms
	djnz	r0, bip_loop
	mov	pwm1, #0
	pop	0
	ret
		 
;----------------------------------------
;  Routines I2C 
;----------------------------------------
; "I2C_Start" :

I2C_Start:       SETB       SDA            ; 
                 SETB       SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 CLR        SDA            ; 
                 NOP                       ; 
                 NOP                       ; 
                 CLR        SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 SETB       SDA            ; 
                 RET                       ; 

; "I2C_Stop" :

I2C_Stop:        CLR        SCL            ; 
                 CLR        SDA            ;
                 NOP                       ; 
                 NOP                       ; 
                 SETB       SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 SETB       SDA            ; 
                 NOP                       ; 
                 NOP                       ; 
                 RET                       ; 

; "I2C_WR_Byte" :

I2C_WR_Byte:     PUSH       ACC            ; 
                 PUSH       0              ; 
                 CLR        SCL            ; 
                 MOV        R0,#8          ; 
i2cwrb:          RLC        A              ; 
                 MOV        SDA,C          ; 
                 NOP                       ; 
                 SETB       SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 CLR        SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 DJNZ       R0,i2cwrb      ; 
                 SETB       SDA            ; 
                 POP        0              ; 
                 POP        ACC            ; 
                 RET                       ; 

; "I2C_RD_Byte" :

I2C_RD_Byte:     PUSH       0              ; 
                 CLR        SCL            ; 
                 SETB       SDA            ; 
                 MOV        R0,#8          ; 
i2crdb:          SETB       SCL            ; 
                 NOP                       ; 
                 MOV        C,SDA          ; 
                 RLC        A              ; 
                 CLR        SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 DJNZ       R0,i2crdb      ; 
                 POP        0              ; 
                 RET                       ; 

; "I2C_WR_ACK" :

I2C_WR_ACK:      CLR        SCL            ; 
                 CLR        SDA            ; 
                 NOP                       ; 
                 SETB       SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 CLR        SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 SETB       SDA            ; 
                 RET                       ; 

; "I2C_WR_NO_ACK" :

I2C_WR_NO_ACK:   CLR        SCL            ; 
                 SETB       SDA            ; 
                 NOP                       ; 
                 SETB       SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 CLR        SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 RET                       ; 

; "I2C_RD_ACK" : lit l'ACK renvoy� par le p�riph�rique adress� en �criture ;
; si le p�riph�rique r�pond, alors "I2C_ACK" est mis � 1 ; si "I2C_ACK" est
; � 0 � l'issue de la routine, c'est que le p�riph�rique �tait indisponible.

I2C_RD_ACK:      CLR        SCL            ; 
                 SETB       SDA            ; 
                 NOP                       ; 
                 SETB       SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 MOV        C,SDA          ; 
                 CPL        C              ; 
                 MOV        I2C_ACK,C      ; 
                 CLR        SCL            ; 
                 NOP                       ; 
                 NOP                       ; 
                 RET                       ; 

; "I2C_RD_Page" : Transf�re 16 octets de l'EEPROM vers la RAM externe.
; Param�tres � fournir : "Page" et "DPTR".
; La variable "Page" contient le num�ro (de $00 � $7F) de la page de 16 
; octets � lire dans l'EEPROM ; le registre DPTR pointe sur la premi�re 
; adresse de la zone de 16 octets en RAM externe, o� les donn�es lues seront 
; plac�es : cette zone va donc de l'adresse [DPTR+0] � l'adresse [DPTR+15] ;
; � la fin de la routine, DPTR a �t� augment� de 16.
; Code d'erreur renvoy� dans "I2C_err" : 0 si tout est OK sinon 1, 2 ou 3.

I2C_RD_Page:     PUSH       ACC            ; 
                 PUSH       1              ; 
                 CALL      wdt_reset  ; 
                 CALL       I2C_Start      ; 
                 MOV        A,Page         ; 
                 SWAP       A              ; 
                 RL         A              ; 
                 ANL        A,#00001110b   ; 
                 ORL        A,#0A0h        ; 
                 CALL       I2C_WR_Byte    ; 
                 CALL       I2C_RD_ACK     ; 
                 JB         I2C_ACK,OKrd1  ; 
                 MOV        I2C_err,#1     ; 
                 JMP        finrdpage      ; 
OKrd1:           MOV        A,Page         ; 
                 SWAP       A              ; 
                 ANL        A,#11110000b   ; 
                 CALL       I2C_WR_Byte    ; 
                 CALL       I2C_RD_ACK     ; 
                 JB         I2C_ACK,OKrd2  ; 
                 MOV        I2C_err,#2     ; 
                 JMP        finrdpage      ; 
OKrd2:           CALL       I2C_Start      ; 
                 MOV        A,#0A1h        ; 
                 CALL       I2C_WR_Byte    ; 
                 CALL       I2C_RD_ACK     ; 
                 JB         I2C_ACK,OKrd3  ; 
                 MOV        I2C_err,#3     ; 
                 JMP        finrdpage      ; 
OKrd3:           MOV        R1,#16         ; 
rdnextbyte:      CALL       I2C_RD_Byte    ; 
                 MOVX       @DPTR,A        ; 
                 INC        DPTR           ; 
                 DJNZ       R1,rp_notlast  ; 
                 JMP        rp_last        ; 
rp_notlast:      CALL       I2C_WR_ACK     ; 
                 JMP        rdnextbyte     ; 
rp_last:         CALL       I2C_WR_NO_ACK  ; 
                 MOV        I2C_err,#0     ; 
finrdpage:       CALL       I2C_Stop       ; 
                 POP        1              ; 
                 POP        ACC            ; 
                 RET                       ; 

; "I2C_WR_Page" : Programme 16 octets de la RAM externe dans l'EEPROM.
; Param�tres � fournir : "Page" et "DPTR".
; La variable "Page" contient le num�ro (de $00 � $7F) de la page de 16 
; octets � programmer dans l'EEPROM ; le registre DPTR pointe sur la premi�re
; adresse de la zone de 16 octets en RAM externe, o� les donn�es � programmer
; dans l'EEPROM seront pr�lev�es ; � la fin, DPTR a donc �t� augment� de 16.

I2C_WR_Page:     PUSH       ACC            ; 
                 PUSH       1              ; 
                 LCALL      wdt_reset  ; 
                 CALL       I2C_Start      ; 
                 MOV        A,Page         ; 
                 SWAP       A              ; 
                 RL         A              ; 
                 ANL        A,#00001110b   ; 
                 ORL        A,#0A0h        ; 
                 CALL       I2C_WR_Byte    ; 
                 CALL       I2C_RD_ACK     ; 
                 JB         I2C_ACK,OKwr1  ; 
                 MOV        I2C_err,#4     ; 
                 JMP        finwrpage      ; 
OKwr1:           MOV        A,Page         ; 
                 SWAP       A              ; 
                 ANL        A,#11110000b   ; 
                 CALL       I2C_WR_Byte    ; 
                 CALL       I2C_RD_ACK     ; 
                 JB         I2C_ACK,OKwr2  ; 
                 MOV        I2C_err,#5     ; 
                 JMP        finwrpage      ; 
OKwr2:           MOV        R1,#16         ; 
wrnextbyte:      MOVX       A,@DPTR        ; 
                 INC        DPTR           ; 
                 CALL       I2C_WR_Byte    ; 
                 CALL       I2C_RD_ACK     ; 
                 JB         I2C_ACK,OKwr3  ; 
                 MOV        I2C_err,#6     ; 
                 JMP        finwrpage      ; 
OKwr3:           DJNZ       R1,wrnextbyte  ; 
                 MOV        I2C_err,#0     ; 
finwrpage:       CALL       I2C_Stop       ; 
                 CALL       Tempo2ms       ; 
                 CALL       Tempo2ms       ; 
                 CALL       Tempo2ms       ; 
                 CALL       Tempo2ms       ; 
                 CALL       Tempo2ms       ; 
                 POP        1              ; 
                 POP        ACC            ; 
                 RET


	