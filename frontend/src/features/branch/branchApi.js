import api from '../../services/api';

export const branchApi = {
  listActive: () => api.get('/branches'),
};
