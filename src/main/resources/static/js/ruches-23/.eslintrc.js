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
	"plugins": [
		"html"
	],
	"rules": {
		"array-callback-return": "error",
		"arrow-parens": ["error", "as-needed"],
		"block-scoped-var": "error",
		"curly": "error",
		"dot-notation": "error",
		"eqeqeq": "error",
		"for-direction": "error",
		"func-style": ["error", "declaration"],
		"no-console": "error",
		"no-constant-binary-expression": "error",
		"no-eval": "error",
		// "no-extra-parens": "error",
		"no-extra-semi": "error",
		"no-floating-decimal": "error",
		"no-lonely-if": "error",
		"no-negated-condition": "error",
		"no-self-compare": "error",
		"no-template-curly-in-string": "error",
		"no-unmodified-loop-condition": "error",
		"no-unreachable-loop": "error",
		"no-unused-expressions": "error",
		"no-unused-vars": ["error", { "args": "after-used" }],
		"no-unneeded-ternary": "error",
		"no-useless-return": "error",
		"no-var": "error",
		"prefer-const": "error",
		"quotes": ["error", "single", { "avoidEscape": true }],
		"quote-props": ["error", "as-needed"],
		"semi-style": "error",
	}
}
