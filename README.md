# Go-DomDistiller

Go-DomDistiller is a Go package that finds the main readable content and the metadata from a HTML page. It works by removing clutter like buttons, ads, background images, script, etc.

This package is based on [DOM Distiller][0] which is part of the Chromium project that is built using Java language. The structure of this package is arranged following the structure of original Java code. This way, any improvements from Chromium can be implemented easily here. Another advantage, hopefully all web page that can be parsed by the original Dom Distiller can be parsed by this package as well with identical result.

## Status

This package is still in development and the port process is still not finished. There are 134 files with 14,059 lines of code that haven’t been ported, so there is still long way to go.

## Changelog

### 6 October 2020

- Port `CreateDivTree` from `TestUtil.java`

### 5 October 2020

- Port `SchemaOrgParser` and `SchemaOrgParserAccessor` from `SchemaOrg.java`
- Port `MarkupParser` from `MarkupParser.java`
- Port `getDocumentTitle` from `DocumentTitleGetter.java`

### 4 October 2020

- Port `IEReadingViewParser` from `IEReadingViewParser.java`

### 3 October 2020

- Porting process started
- Port `WordCounter` interface from `StringUtil.java`
- Port `OpenGraphParser` and `OpenGraphParserAccessor` from `OpenGraphParser.java`

[0]: https://chromium.googlesource.com/chromium/dom-distiller
