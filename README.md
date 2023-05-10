# lwhorton.github.io

I use ruby + jekyll to write/build/compile a simple static blog: https://jekyllrb.com/

Your computer probably already has ruby on it, and i think it comes packaged
with [bundler](https://bundler.io/) (used for builds) these days. If it doesn't,
check out the [install](https://jekyllrb.com/docs/) instructions. You probably
also want some sort of env manager so you aren't globally installing ruby and
screwing up some other thing; i use [asdf](https://github.com/asdf-vm/asdf-ruby).

you probably need `brew install ruby-build` for any of the asdf ruby versions to
install and build properly. brew is slow, but it's the best we've got, sorry.

finally, to host the site locally:

`bundle exec jekyll serve`


