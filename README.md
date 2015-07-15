# blogger2cryogen

Small utility to easily migrate [Blogger](http://blogger.com) content to [Cryogen](http://cryogenweb.org).

## Usage

### Compiling blogger2cryogen

These are the steps on how to get blogger2cryogen up and running:

1) make sure you have Java and [Leiningen](http://leiningen.org) installed
2) clone this repository
3) in _blogger2cryogen_ folder, run `lein uberjar`

By now, you should have it successfully compiled. To validate, run:

```
./bin/blogger2cryogen
```

script. You will get help output like this:

```
Usage: blogger2cryogen [options]
Export your Blogger posts to Cryogen static site generator.
  -i, --in  DUMP    XML dump from Blogger
  -o, --out FOLDER  Extract posts to FOLDER
  -n, --no-struct   Do not create FOLDER/pages and FOLDER/posts subfolders
  -t, --tidy        Tidy extracted HTML
```

### Exporting Blogger data

Before you initiate conversion, you will need to export Blogger posts on
your disk. [Export or import your blog](https://support.google.com/blogger/answer/97416?hl=en)
describes the steps how to do it.

### Importing into Cryogen

Make sure to install
[cryogen-html](https://github.com/sanel/cryogen-html) plugin first,
either in your running Cryogen blog or freshly created. This plugin
will give your blog ability to render HTML posts, without additional
conversion.

Inside `<YOUR-BLOG>/resources/templates` create folder `html` with:

```
mkdir <YOUR-BLOG>/resources/templates/html
```

and run _blogger2cryogen_ with:

```
./bin/blogger2cryogen -i <BLOGGER-XML-DUMP> -o <YOUR-BLOG>/resources/templates/html -t
```

## Reporting bugs

In case you notice something is wrong or you have suggestions, feel
free to [let me know](https://github.com/sanel/blogger2cryogen/issues).

## License

Copyright © 2015 Sanel Zukan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
