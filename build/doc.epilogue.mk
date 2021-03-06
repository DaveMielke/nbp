html: $(DOCUMENT_NAMES:=.html)
txt: $(DOCUMENT_NAMES:=.txt)

%.html: %/main.rst $(LOCAL_FILES)
	rst2html --config $(BUILD_DIRECTORY)docutils.conf $< $@

%.txt: %.html
	lynx -dump -stderr -width=10000 -dont_wrap_pre -nolist -nonumbers $< >$@
	sed -e '/^ *Contents *$$/,/^ *$$/d' -i $@
#	links -dump -dump-charset utf-8 -no-references -no-numbering $< >$@
#	html2text -rcfile $(BUILD_DIRECTORY)html2text.conf -nobs -o $@ $<

clean:
	-rm -f -- *.html
	-rm -f -- *.txt
	-rm -f -- $(LOCAL_FILES)

