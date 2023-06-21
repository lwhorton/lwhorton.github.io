# lwhorton.github.io

I use ruby + jekyll to write/build/compile a simple static blog: https://jekyllrb.com/

Your computer probably already has ruby on it, and i think it comes packaged
with [bundler](https://bundler.io/) (used for builds) these days. If it doesn't,
check out the [install](https://jekyllrb.com/docs/) instructions. You probably
also want some sort of env manager so you aren't globally installing ruby and
screwing up some other thing; i use [asdf](https://github.com/asdf-vm/asdf-ruby).

you probably need `brew install ruby-build` for any of the asdf ruby versions to
install and build properly. because ruby and gems are finicky, you also probably
need to update the bundler after asdf installing the ruby version. ruby,
bundler, and gem versions are a mess, good luck:

```
gem install bundler -v '<2.0'
bundle # installs all the Gemfile info to .bundle/
```

You also also probably can't use anything ruby `>3` with Jekyll. finally, to
host the site locally:

`bundle exec jekyll serve`

# pre-deploy

we're using github's free jekyll site deployer/builder thing. pushing to `main`
will build and redeploy a new version of the site. before you do that, you might
want to spellcheck and html-validate. currently this is run locally because
setting up CI/CD automation, even with gh-actions, is time consuming.

```
brew install tidy-html5
brew install aspell

for f in *.html ; do echo $f ; aspell list < $f | sort | uniq -c ; done
```


