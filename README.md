## Description

This is a fully functioning blog that serves as an example web app using datomic and noir.  While
the models and queries are simple, the goal is to show how easy interacting with datomic can be with
[datomic-simple](http://github.com/cldwalker/datomic-simple). For a before/after comparison of what
changed, [view the diff](https://github.com/cldwalker/datomic-noir-blog/compare/upgrades...master).

## Usage

```bash
# Until datomic-simple is a clojar or lein-git-deps works with this app
$ git clone https://github.com/cldwalker/datomic-noir-blog.git; cd datomic-noir-blog; lein install;

$ lein deps
$ lein run
```

To login, an initial user is created:

username :: admin
password :: admin

## Credits

* @bobby - for [his helpful datomic helpers](https://gist.github.com/3150938)
* @relevance - for awesome fridays that gave me time to tackle this

## License

Copyright (C) 2012 Gabriel Horner

Distributed under the Eclipse Public License, the same as Clojure.
