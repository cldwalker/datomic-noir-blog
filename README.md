## Description

This is a fully functioning blog that serves as an example web app using datomic and noir.
While the models and queries are simple, the goal is to show how easy interacting with datomic can
be given [the right helpers](#TODO). For a before/after comparison of what changed, [view the
diff](#TODO). One difference you'll notice in datomic helpers here is that database creating
and querying are namespace independent. This means that instead of model code with
:user/username and :post/title you can use :username and :title.

## Usage

```bash
lein deps
lein run
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
