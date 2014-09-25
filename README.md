# jmxpush

A Clojure clone of riemann-jmx.

## Building

Use `lein uberjar` to build the standalone jar.

## Usage

Pass each of the jmxpush.yml as command line options, e.g.:

```
java -jar jmxpush-standalone.jar jvm-config-1.yml jvm-config-2.yml jvm-config-3.yml
```
See jmxpush.yml.example for an example of how to write a configuration file.

## License

Copyright © 2014 https://github.com/xxbbxb/jmxpush

Distributed under the Eclipse Public License version 1.0
