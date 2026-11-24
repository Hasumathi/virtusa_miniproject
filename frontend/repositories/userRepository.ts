
import { UserRole } from "@prisma/client";
import { prisma } from "../lib/prisma";

interface CreateUserInput {
  name: string;
  identity: string;
  email: string;
  passwordHash: string;
  role: UserRole;
}

export interface UpdateProfileInput {
  name?: string;
  phone?: string | null;
  department?: string | null;
  bio?: string | null;
  profileImageUrl?: string | null;
}

export interface UpdateSettingsInput {
  emailNotifications?: boolean;
  pushNotifications?: boolean;
}

export async function createUser(data: CreateUserInput) {
  return prisma.user.create({
    data: {
      name: data.name,
      identity: data.identity,
      email: data.email,
      passwordHash: data.passwordHash,
      role: data.role,
      profileSettings: {
        create: {},
      },
    },
    include: {
      profileSettings: true,
    },
  });
}

export async function findUserByEmail(email: string) {
  return prisma.user.findUnique({
    where: { email },
    include: {
      profileSettings: true,
    },
  });
}

export async function findUserByIdentity(
  identity: string,
  role: UserRole
) {
  return prisma.user.findFirst({
    where: {
      identity,
      role,
    },
    include: {
      profileSettings: true,
    },
  });
}

export async function findUserById(id: string) {
  return prisma.user.findUnique({
    where: { id },
    include: {
      profileSettings: true,
    },
  });
}

export async function updateUserProfile(
  userId: string,
  data: UpdateProfileInput
) {
  const { name, phone, department, bio, profileImageUrl } = data;

  return prisma.user.update({
    where: { id: userId },
    data: {
      ...(name !== undefined ? { name } : {}),
      profileSettings: {
        upsert: {
          create: {
            phone: phone ?? null,
            department: department ?? null,
            bio: bio ?? null,
            profileImageUrl: profileImageUrl ?? null,
          },
          update: {
            ...(phone !== undefined ? { phone } : {}),
            ...(department !== undefined ? { department } : {}),
            ...(bio !== undefined ? { bio } : {}),
            ...(profileImageUrl !== undefined
              ? { profileImageUrl }
              : {}),
          },
        },
      },
    },
    include: {
      profileSettings: true,
    },
  });
}

export async function updateUserSettings(
  userId: string,
  data: UpdateSettingsInput
) {
  return prisma.profileSettings.upsert({
    where: { userId },
    create: {
      userId,
      emailNotifications: data.emailNotifications ?? true,
      pushNotifications: data.pushNotifications ?? true,
    },
    update: {
      ...(data.emailNotifications !== undefined
        ? { emailNotifications: data.emailNotifications }
        : {}),
      ...(data.pushNotifications !== undefined
        ? { pushNotifications: data.pushNotifications }
        : {}),
    },
  });
}
