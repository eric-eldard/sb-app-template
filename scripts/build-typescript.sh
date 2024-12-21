mkdir -p out/min

# https://www.npmjs.com/package/typescript
# https://www.npmjs.com/package/browserify
node_modules/.bin/tsc && node_modules/.bin/browserify out/bundler.js -o out/app.bundle.js

# https://www.npmjs.com/package/uglify-js
node_modules/.bin/uglifyjs out/app.bundle.js \
  --output out/min/app.bundle.min.js \
  --source-map "base=out,url=app.bundle.min.js.map" \
  --mangle \
  --compress

node_modules/.bin/uglifyjs out/utils.js \
  --output out/min/utils.min.js \
  --source-map "base=out,url=utils.min.js.map" \
  --mangle \
  --compress