module.exports = {
    "env": {
        "browser": true,
        "es2021": true,
        "jquery": true
    },
    "extends": "eslint:recommended",
    "overrides": [
        {
            "env": {
                "node": true
            },
            "files": [
                ".eslintrc.{js,cjs}"
            ],
            "parserOptions": {
                "sourceType": "script"
            }
        }
    ],
    "parserOptions": {
        "ecmaVersion": "latest",
        "impliedStrict": true
    },
    "rules": {
    	"quotes": ["error", "single"],
    	"no-self-compare": "error",
    	"no-constant-binary-expression": "error",
    	"array-callback-return": "error"
    }
}
