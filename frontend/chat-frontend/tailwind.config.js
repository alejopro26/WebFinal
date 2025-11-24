export default {
    content: [
        "./index.html",
        "./src/**/*.{js,jsx,ts,tsx}"
    ],
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                discord: {
                    900: '#0f1720',
                    800: '#121318',
                    700: '#1f1f29',
                    blurple: '#5865F2',
                    violet: '#7c5cff',
                    bg: '#0f1115',
                    card:'#1e1f23'
                }
            },
            fontFamily: {
                sans: ['Inter', 'ui-sans-serif', 'system-ui']
            }
        }
    },
    plugins: []
}
