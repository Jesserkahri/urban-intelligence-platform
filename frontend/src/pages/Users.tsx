import { fetchUsers } from '@/services/user'
import { User } from '@/types/api';
import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { Badge } from "@components/ui/Badge";
import { SEVERITY_COLORS, STATUS_COLORS } from "@lib/utils";
import { Button } from '@/components/ui/Button';
import { useNavigate } from 'react-router-dom';
import { Edit, Trash } from 'lucide-react';

const Users = () => {
  const [users,setUsers] = useState<User>([])
  const [loading,setLoading] = useState(true)
  const [page, setPage] = useState(1);
  const navigate = useNavigate()

    useEffect(() => {
  const loadUsers = async () => {
    const users = await fetchUsers();
    setUsers(users);
    setLoading(false)
  };

  loadUsers();
}, []);
  const itemsPerPage = 10;
  const paginatedIncidents = users.slice(
    (page - 1) * itemsPerPage,
    page * itemsPerPage,
  );
  const totalPages = Math.ceil(users.length / itemsPerPage);
if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Incidents</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded" />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }
   return (
      <Card>
        <CardHeader>
          <div className='flex justify-between'>
          <CardTitle>Users</CardTitle>
          <Button onClick={()=>navigate('add')}>Add user</Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-4 font-medium">Username</th>
                  <th className="text-left py-3 px-4 font-medium">Email</th>
                  <th className="text-left py-3 px-4 font-medium">Role</th>
                  <th className="text-left py-3 px-4 font-medium">Created</th>
                  <th className="text-left py-3 px-4 font-medium">Action</th>
                </tr>
              </thead>
              <tbody>
                {paginatedIncidents.length === 0 ? (
                  <tr>
                    <td
                      colSpan={4}
                      className="text-center py-8 text-muted-foreground"
                    >
                      No incidents found
                    </td>
                  </tr>
                ) : (
                  paginatedIncidents.map((user) => (
                    <tr
                      key={user.id}
                      className="border-b border-border hover:bg-muted/50"
                    >
                      <td className="py-3 px-4">{user.username}</td>
                      <td className="py-3 px-4">
                       {user.email}
                      </td>
                      <td className="py-3 px-4">
                        <Badge
                          className={
                            STATUS_COLORS[
                              user.status as keyof typeof STATUS_COLORS
                            ]
                          }
                        >
                          {user.role}
                        </Badge>
                      </td>
                      <td className="py-3 px-4 text-muted-foreground">
                        {new Date(user.createdAt).toLocaleDateString()}
                      </td>
                      <td>
                        <Badge className='me-2 bg-red-600'>
                          <Trash />
                        </Badge>
                        <Badge>
                          <Edit />
                        </Badge>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
  
            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between mt-4 pt-4 border-t border-border">
                <p className="text-sm text-muted-foreground">
                  Page {page} of {totalPages}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage(Math.max(1, page - 1))}
                    disabled={page === 1}
                    className="px-3 py-1 rounded text-sm border border-border hover:bg-accent disabled:opacity-50"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => setPage(Math.min(totalPages, page + 1))}
                    disabled={page === totalPages}
                    className="px-3 py-1 rounded text-sm border border-border hover:bg-accent disabled:opacity-50"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    );
  };
  

export default Users