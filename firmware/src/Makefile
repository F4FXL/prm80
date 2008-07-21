all: prm8060

debug: prm8060debug

prm8060: prm.a51
	asem -v --define=TARGET:8060 prm.a51
	hexbin prm.hex --fill=ff --length=20000 -v

prm8060debug:
	asem -v --define=TARGET:8060 prm.a51
	hexbin prm.hex -v

clean:
	rm -rf *.lst

mrproper: clean
	rm -rf *.bin *.hex