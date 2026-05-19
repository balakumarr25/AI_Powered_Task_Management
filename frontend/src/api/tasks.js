import client from './client';

export const fetchTasks = () => client.get('/api/tasks');
export const createTask = (data) => client.post('/api/tasks', data);
export const updateTask = (id, data) => client.put(`/api/tasks/${id}`, data);
export const updateTaskStatus = (id, status) =>
  client.patch(`/api/tasks/${id}/status`, { status });
export const deleteTask = (id) => client.delete(`/api/tasks/${id}`);
export const fetchLedger = (taskId) => client.get(`/api/tasks/${taskId}/ledger`);
