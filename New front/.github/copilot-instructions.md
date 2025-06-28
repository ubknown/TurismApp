# GitHub Copilot Instructions

<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

## Project Overview
This is a React + Vite frontend for a tourism management application with a stunning glassmorphism design featuring a starry night sky background.

## Design System
- **Background**: Full-screen starry night sky with purple/blue gradient and forest/mountain silhouettes
- **Components**: Glassmorphism style (bg-white/10, backdrop-blur-xl), soft shadows, rounded corners
- **Colors**: Violet, indigo, blue, white-transparent palette
- **Typography**: Inter or Poppins font family
- **Animations**: Fade-in, hover glow, subtle motion on cards
- **Icons**: Lucide React icons, properly scaled (w-6 or w-8 max)
- **Responsive**: Mobile-first design approach

## Code Standards
- Use TailwindCSS utility classes exclusively (no external CSS)
- Implement glassmorphism with `bg-white/10 backdrop-blur-xl`
- Follow the established folder structure in src/
- Use functional components with React hooks
- Implement proper TypeScript types where applicable
- Use React Router v6 for navigation
- Maintain consistent spacing and hover effects

## Architecture
- Components are modular and reusable
- Pages handle routing and data fetching
- Context provides global state management
- Layouts provide consistent structure across pages
- Utils contain helper functions and constants

## Component Guidelines
- All cards should use glassmorphism styling
- Buttons should have hover glow effects
- Input fields should be translucent with proper focus states
- Use consistent spacing (p-6, m-4, gap-4, etc.)
- Implement smooth transitions with duration-300
