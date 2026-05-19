import client from './client';

/**
 * Generate task details from a title.
 * @param {string} title
 * @param {string|null} provider - "openai" | "gemini" | "huggingface" | "auto" | null
 */
export const generateTaskDetails = (title, provider = null) =>
  client.post('/api/ai/generate-task', { title, provider });

/**
 * Fetch which AI providers are configured on the backend.
 * Returns: { openai: bool, gemini: bool, huggingface: bool, configured: string, models: {...} }
 */
export const fetchAiProviders = () =>
  client.get('/api/ai/providers');
