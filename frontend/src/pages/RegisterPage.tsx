import React, { useState } from "react";
import { Link,useNavigate } from "react-router-dom";
import { useAuth } from "@context/auth";
import { Button } from "@components/ui/Button";
import { Input } from "@components/ui/Input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@components/ui/Card";

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register, isLoading, error, clearError } = useAuth();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [localError, setLocalError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);
    clearError();

    if (!username || !password || !email) {
      setLocalError("Username and password and email are required");
      return;
    }

    try {
      await register(username,email ,password);
      navigate("/login", { replace: true });
    } catch (err) {
      setLocalError(error || "Login failed. Please try again.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 p-4">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-bold text-white mb-2">Urban Flagship</h1>
          <p className="text-slate-400">Urban Intelligence Platform</p>
        </div>

        {/* Login Card */}
        <Card className="border-slate-700 bg-slate-800">
          <CardHeader>
            <CardTitle className="text-white">Create an Account</CardTitle>
            <CardDescription className="text-slate-400">
              Fill in your information to create your account.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Error Message */}
              {(localError || error) && (
                <div className="p-3 rounded-md bg-red-900/20 border border-red-800 text-red-200 text-sm">
                  {localError || error}
                </div>
              )}

              {/* Username */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-200">
                  Username
                </label>
                <Input
                  type="text"
                  placeholder="Enter your username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={isLoading}
                  className="bg-slate-700 border-slate-600 text-white placeholder:text-slate-400"
                />
              </div>

               {/* Username */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-200">
                  Email
                </label>
                <Input
                  type="text"
                  placeholder="Enter your Email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                  className="bg-slate-700 border-slate-600 text-white placeholder:text-slate-400"
                />
              </div>


              {/* Password */}
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-200">
                  Password
                </label>
                <Input
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={isLoading}
                  className="bg-slate-700 border-slate-600 text-white placeholder:text-slate-400"
                />
              </div>

              {/* Submit Button */}
              <Button type="submit" disabled={isLoading} className="w-full">
                {isLoading ? "Registering..." : "Register"}
              </Button>
              <div className="flex justify-center">
                <p>have you account? 
                <Link className=" text-blue-500" to={"/login"}> Sign in</Link>
                </p>
                </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
