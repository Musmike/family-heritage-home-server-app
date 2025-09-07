#!/bin/sh

ROOT_DIR=/usr/share/nginx/html

echo "Replacing placeholder with API URL: ${VITE_API_BASE_URL}"
for file in $ROOT_DIR/assets/*.js* $ROOT_DIR/index.html;
do
  sed -i 's|__VITE_API_BASE_URL__|'${VITE_API_BASE_URL}'|g' $file
done

exec "$@"