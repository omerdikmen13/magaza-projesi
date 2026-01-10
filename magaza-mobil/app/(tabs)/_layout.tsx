import React from 'react';
import { Tabs } from 'expo-router';
import { Home, Store, ShoppingCart, User, Shield, Heart } from 'lucide-react-native';
import { useAuthStore } from '../../stores/authStore';

export default function TabLayout() {
  const { user, isAdmin, isMagazaSahibi } = useAuthStore();

  // Admin için özel layout
  if (isAdmin()) {
    return (
      <Tabs
        screenOptions={{
          headerShown: false,
          tabBarStyle: {
            backgroundColor: '#1a1a2e',
            borderTopWidth: 0,
            height: 70,
            paddingBottom: 10,
            paddingTop: 10,
            elevation: 10,
            shadowColor: '#000',
            shadowOffset: { width: 0, height: -4 },
            shadowOpacity: 0.3,
            shadowRadius: 8,
          },
          tabBarActiveTintColor: '#d946ef',
          tabBarInactiveTintColor: '#6b7280',
          tabBarLabelStyle: {
            fontSize: 12,
            fontWeight: '600',
          },
        }}
      >
        <Tabs.Screen
          name="index"
          options={{
            title: 'Ana Sayfa',
            tabBarIcon: ({ color, size }) => <Home size={size} color={color} />,
          }}
        />
        <Tabs.Screen
          name="magazalar"
          options={{
            title: 'Mağazalar',
            tabBarIcon: ({ color, size }) => <Store size={size} color={color} />,
          }}
        />
        <Tabs.Screen
          name="sepet"
          options={{
            href: null, // Admin için sepeti gizle
          }}
        />
        <Tabs.Screen
          name="favoriler"
          options={{
            href: null, // Admin için favorileri gizle
          }}
        />
        <Tabs.Screen
          name="hesabim"
          options={{
            title: 'Admin',
            tabBarIcon: ({ color, size }) => <Shield size={size} color={color} />,
          }}
        />
      </Tabs>
    );
  }

  // Normal kullanıcı için layout
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: '#ffffff',
          borderTopWidth: 1,
          borderTopColor: '#e2e8f0',
          height: 70,
          paddingBottom: 10,
          paddingTop: 10,
          elevation: 10,
          shadowColor: '#000',
          shadowOffset: { width: 0, height: -4 },
          shadowOpacity: 0.1,
          shadowRadius: 8,
        },
        tabBarActiveTintColor: '#667eea',
        tabBarInactiveTintColor: '#a0aec0',
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '600',
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: 'Ana Sayfa',
          tabBarIcon: ({ color, size }) => <Home size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="magazalar"
        options={{
          title: 'Mağazalar',
          tabBarIcon: ({ color, size }) => <Store size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="sepet"
        options={{
          title: 'Sepetim',
          tabBarIcon: ({ color, size }) => <ShoppingCart size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="favoriler"
        options={{
          title: 'Favoriler',
          tabBarIcon: ({ color, size }) => <Heart size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="hesabim"
        options={{
          title: 'Hesabım',
          tabBarIcon: ({ color, size }) => <User size={size} color={color} />,
        }}
      />
    </Tabs>
  );
}
