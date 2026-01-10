/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{js,jsx,ts,tsx}", "./components/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        // Web'den çıkarılan renkler
        primary: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a3bffa',
          400: '#818cf8',
          500: '#667eea', // Web primary
          600: '#5a67d8',
          700: '#4c51bf',
          800: '#434190',
          900: '#3c366b',
        },
        secondary: {
          400: '#9f7aea',
          500: '#764ba2', // Web secondary  
          600: '#6b46c1',
        },
        accent: {
          400: '#f5a3c7',
          500: '#f093fb', // Web accent
          600: '#ed64a6',
        },
        // Durum renkleri (web ile aynı)
        success: '#38a169',
        warning: '#d69e2e',
        danger: '#e53e3e',
        info: '#3182ce',
        // Nötr renkler (açık tema - web ile aynı)
        dark: {
          100: '#f7fafc',
          200: '#edf2f7',
          300: '#e2e8f0',
          400: '#cbd5e0',
          500: '#a0aec0',
          600: '#718096',
          700: '#4a5568',
          800: '#2d3748',
          900: '#1a202c',
        },
        gray: {
          50: '#f9fafb',
          100: '#f7fafc',
          200: '#edf2f7',
          300: '#e2e8f0',
          400: '#cbd5e0',
          500: '#a0aec0',
          600: '#718096',
          700: '#4a5568',
          800: '#2d3748',
          900: '#1a1f36',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
