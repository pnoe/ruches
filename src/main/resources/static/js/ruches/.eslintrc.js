module.exports = {
    "env": {
        "browser": true,
        "es2023": true,
        "jquery": true
    },
    "extends": "eslint:recommended",
    "parserOptions": {
        "ecmaVersion": "latest",
        "impliedStrict": true
    },
    "rules": {
    	"array-callback-return": "error",
    	"eqeqeq": "error",
    	"func-style": ["error", "declaration"],
    	"no-constant-binary-expression": "error",
    	"no-eval": "error",
    	"no-self-compare": "error",
    	"no-negated-condition": "error",
    	"no-unmodified-loop-condition": "error",
    	"no-unused-expressions": "error",
    	"no-unneeded-ternary": "error",
    	"no-useless-return": "error",
    	"no-var": "error",
    	"prefer-const": "error",
    	"quotes": ["error", "single", {"avoidEscape": true }],
    	"quote-props": ["error", "as-needed"],
    }
}
