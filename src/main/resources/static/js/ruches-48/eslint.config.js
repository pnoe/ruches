const htmlPlugin = require('eslint-plugin-html')

module.exports = [
	{
		languageOptions: {
			ecmaVersion: 'latest',
			sourceType: 'script',
		},
		plugins: {
			html: htmlPlugin
		},
		settings: {
			'html/html-extensions"': ['.html']
		},
		rules: {
			'array-callback-return': 'error',
			'arrow-parens': ['error', 'as-needed'],
			'block-scoped-var': 'error',
			curly: 'error',
			'dot-notation': 'error',
			eqeqeq: 'error',
			'for-direction': 'error',
			'func-style': ['error', 'declaration'],
			'no-array-constructor': 'error',
			'no-console': 'error',
			'no-constant-binary-expression': 'error',
			'no-eval': 'error',
			// 'no-extra-parens': 'error',
			'no-extra-semi': 'error',
			'no-floating-decimal': 'error',
			'no-lonely-if': 'error',
			'no-negated-condition': 'error',
			'no-self-compare': 'error',
			'no-template-curly-in-string': 'error',
			'no-unmodified-loop-condition': 'error',
			'no-unreachable-loop': 'error',
			'no-unused-expressions': 'error',
			'no-unused-vars': ['error', { args: 'after-used' }],
			'no-unneeded-ternary': 'error',
			'no-useless-return': 'error',
			'no-var': 'error',
			'prefer-const': 'error',
			quotes: ['error', 'single', { avoidEscape: true }],
			'quote-props': ['error', 'as-needed'],
			'semi-style': 'error',
		}
	}
];
